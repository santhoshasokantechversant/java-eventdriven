# customer-service

| | |
|---|---|
| **Port** | 8084 (call it through the gateway on 8081) |
| **Source** | [`customer-service/`](../../customer-service) |
| **DB schema** | `customer_schema` |
| **Talks to** | user-service and account-service (via gateway), Kafka, SMTP |
| **Owns** | Customers, the customer ↔ user link, location reference data (region → country → state → city) |

## What it does

1. **Customer CRUD**, with search, paging and soft status (`ACTIVE`, `INACTIVE`, `BLOCKED`).
2. **Orchestrates the customer-creation saga.** Saving a customer publishes `customer.created`. user-service then creates a login user and account-service creates a savings account. The HTTP request **waits up to 30 seconds** for both to report back, and rolls everything back if either fails or times out.
3. **Sends the onboarding email** with the username, account number and sign-up link once the saga succeeds.
4. **Safe deletion.** A customer can only be deleted when their account is `CLOSED` with a balance of 0. Deletion publishes `customer.deleted` so the user and account are removed too.
5. **Location lookups** for address forms.

## Code layout

```
controller/  CustomerController, LocationController
service/impl/ CustomerService (CRUD + saga), LocationService
service/CustomerCreationSagaTracker   in-memory saga state (CompletableFuture per correlationId)
service/webclient/ UserWebClientService, AccountsWebclientService   (calls through the gateway)
service/feignclient/ UserServiceClient, AccountsServiceClient
consumer/    CustomerEventConsumer (user.created → store user link), SagaCompletionConsumer (saga results)
config/      KafkaConfig (creates topics), AsyncConfig, EmailService, SecurityConfig, ...
resources/templates/customerOnboard.html
resources/db.migrations/V1__sequenceadd.sql   (customer_no sequence; see Known issues: not picked up by Flyway)
```

## API reference

All endpoints require **Permission** (JWT + role permission at the gateway) unless marked Internal.

### Customers – `/api/v1/customers`

| Method | Path | Summary |
|---|---|---|
| POST | `/create-customer` | Create a customer (runs the saga, waits up to 30 s) |
| GET | `/view-all-customers` | Search + paginate customers |
| GET | `/get-customer-by-id/{id}` | One active customer by customer id |
| GET | `/get-customer-by-user-id/{id}` | Customer linked to a user id |
| PUT | `/{customerNo}` | Update a customer by customer number |
| DELETE | `/delete-customer-by-id/{id}` | Delete a customer (account must be closed, balance 0) |
| GET | `/delete-customer-by-user-id/{id}` | Internal: delete the customer of a user (used when deleting a user) |
| GET | `/users-count` | Count of active customers |
| DELETE | `/email/{email}` | Internal: delete a customer + account by email |

#### POST `/api/v1/customers/create-customer`

Request (`CustomerDto`):
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "9876543210",
  "dateOfBirth": "1992-03-14",
  "address": "12 MG Road",
  "city": "Kochi",
  "state": "Kerala",
  "postalCode": "682001",
  "country": "India"
}
```
| Field | Rules |
|---|---|
| `firstName`, `lastName` | required |
| `email` | required, valid email, must not exist in user-service |
| `phoneNumber` | required, must not exist in user-service |
| `dateOfBirth` | required, `yyyy-MM-dd` |
| `status`, `roleId` | ignored on create (new customers are `ACTIVE`, role is `Customer`) |

What happens (see the [saga diagram](../kafka-events-and-saga.md#customer-creation-saga)):
1. Calls `POST /api/v1/users/check-exist-customer`. If the email or phone is taken → error.
2. Saves the customer with the next `customer_no` from sequence `customer_schema.customer_no_seq`.
3. Publishes `customer.created` with a new `correlationId`.
4. Blocks until both `user.created` **and** `account.created` arrive (success), either `*.failed` arrives, or 30 s pass.
5. On failure or timeout it deletes the customer and publishes `customer.rollback` (user-service deletes the user) and `account-rollback` (account-service deletes the account), then returns 500.
6. On success, returns **201** and then (asynchronously) stores the `userId` on the customer and emails the customer their username, account number, account type and sign-up link.

Response `data` (`CustomerResponseDTO`):
```json
{
  "customerId": "3f2c…", "customerNo": 1005, "firstName": "John", "lastName": "Smith",
  "email": "john.smith@example.com", "phoneNumber": "9876543210", "status": "ACTIVE",
  "createdAt": "2026-09-28T10:15:30", "updatedAt": "2026-09-28T10:15:30",
  "address": "12 MG Road", "city": "Kochi", "state": "Kerala", "country": "India",
  "postalCode": "682001", "userId": null
}
```
Protected by a Resilience4j circuit breaker (`customerController`/`customerService`) and retry. When the breaker is open → 503.

#### GET `/api/v1/customers/view-all-customers`
All query parameters are optional:

| Param | Default | Notes |
|---|---|---|
| `firstName`, `lastName`, `email`, `phoneNumber`, `address`, `city`, `state`, `postalCode`, `country` | – | filters |
| `status` | `ACTIVE` | `ACTIVE`, `INACTIVE`, `BLOCKED` |
| `page` / `size` | `0` / `10` | |
| `sortField` | `CREATED_AT` | `FIRST_NAME`, `LAST_NAME`, `EMAIL`, `CREATED_AT` |
| `sortDirection` | `DESCENDING` | `ASCENDING`, `DESCENDING` (note: not `ASC`/`DESC` like other services) |

Response `data`: `{ "customers": [Customer…], "currentPage", "totalPages", "totalItems", "pageSize" }`

`Customer` fields: `customerId, customerNo, firstName, lastName, email, phoneNumber, dateOfBirth, address, city, state, postalCode, country, status, createdAt, updatedAt, userId`

#### GET `/api/v1/customers/get-customer-by-id/{id}`
Active customer by `customerId` (UUID). 404 if not found.

#### GET `/api/v1/customers/get-customer-by-user-id/{id}`
Customer whose `userId` matches. Used by account-service's customer dashboard.

#### PUT `/api/v1/customers/{customerNo}`
Path uses the numeric **customer number**, not the UUID. Body: same `CustomerDto` as create (all required fields must be sent; it's a full replace).

- `status`: `"ACTIVE"` keeps it active; **any other value sets `INACTIVE`**. It's required: a missing status throws.
- Publishes `customer.updated`, and user-service updates the matching user.
- Response `data`: `CustomerResponseDTO`.

#### DELETE `/api/v1/customers/delete-customer-by-id/{id}`
1. Looks up the customer's account via account-service.
2. Refuses if the account is not `CLOSED` (`Account for the customer is still active.`) or the balance ≠ 0 (`Customer Account balance not 0.`).
3. Publishes `customer.deleted` (user-service deletes the user, account-service deletes the account) and deletes the customer.

Response: `{"status":"success","message":"Customer Deleted Successfully."}`

#### GET `/api/v1/customers/delete-customer-by-user-id/{id}` (Internal)
Called by user-service when an admin deletes a `CUSTOMER` user. Performs the same account checks, deletes, and returns the deleted customer as `CustomerReturnDto`, or `status: error` with the reason. ⚠️ It deletes data but uses GET.

#### GET `/api/v1/customers/users-count`
`count` = number of `ACTIVE` customers. Used by the admin dashboard.

#### DELETE `/api/v1/customers/email/{email}` (Internal)
Deletes the account (via account-service) and then the customer. Returns 204.

### Locations – `/api/v1/customers/locations`

Read-only reference data for address dropdowns.

| Method | Path | Response `data` |
|---|---|---|
| GET | `/get-countries-for-region?regionId=3` | `[{ "id": 101, "name": "India" }, …]` (`regionId` defaults to `3`) |
| GET | `/get-states-for-country/{countryId}` | `[{ "id": 4028, "name": "Kerala" }, …]` |
| GET | `/get-city-for-state/{stateId}` | `[{ "id": 133215, "name": "Kochi" }, …]` |

## Kafka

| Direction | Topic | Payload | What happens |
|---|---|---|---|
| Produces | `customer.created` | `CustomerCreatedEvent` (+ `correlationId`) | Starts the saga |
| Produces | `customer.updated` | `CustomerUpdatedEvent` | user-service syncs the user |
| Produces | `customer.deleted` | `CustomerDeletedEvent` | user-service and account-service delete their records |
| Produces | `customer.rollback` | `UserRollbackEvent` / `CustomerRollbackEvent` | Saga compensation → user-service deletes the user |
| Produces | `account-rollback` | `AccountRollbackEvent` | Saga compensation → account-service deletes the account |
| Consumes | `user.created` (group `customer-service-group`) | `UserCreatedEvent` | Stores the customer ↔ user link (`customer_user` table) |
| Consumes | `user.created`, `user.creation.failed`, `account.created`, `account.creation.failed` (group `customer-service-saga-group`) | | Completes / fails the waiting saga |

`KafkaConfig` creates `customer.created`, `customer.updated`, `customer.deleted`, `customer.rollback`, `user.creation.failed`, `account.creation.failed` with 3 partitions, replication factor 1.

## Configuration

| Env var | Property | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | `spring.datasource.*` | PostgreSQL |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | JWT issuer | |
| `KEYCLOAK_*` | `keycloak.*` | Present in config; not used by customer logic |
| `SMTP_MAIL_USERNAME`, `SMTP_MAIL_PASSWORD` | `spring.mail.*` | Onboarding email |
| `REGISTER_FRONTEND_URL` | `service.register-url` | Sign-up link in the email |
| `USER_SERVICE_URL` | `user.service.url` | Gateway URL for WebClient calls (default `http://gateway-service:8081`) |
| `MAIN_SERVICE_URL` | `main.service.url` | Gateway URL for Feign clients |

Resilience4j instance `customerService`: 3 retries (2 s, exponential ×2), circuit breaker opens at 50% failures over 10 calls for 5 s, 10 s time limit.

## Email template

`customerOnboard.html`, subject "Account Has Been Created". Placeholders: `name`, `userName`, `email`, `phoneNumber`, `accountNumber`, `accountType`, `link`.
