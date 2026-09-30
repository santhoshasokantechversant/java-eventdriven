# account-service

| | |
|---|---|
| **Port** | 8083 (call it through the gateway on 8081) |
| **Source** | [`account-service/`](../../account-service) |
| **DB schema** | `account_schema` |
| **Talks to** | customer-service (via gateway), Kafka |
| **Owns** | Bank accounts, currencies, the admin dashboard figures |

## What it does

1. **Account CRUD.** Accounts are created automatically by the customer saga, or manually through the API.
2. **Saga participant.** When `user.created` arrives, opens a `SAVINGS` account with a 1000.00 INR opening balance and publishes `account.created`, or `account.creation.failed` on error.
3. **Admin dashboard.** Returns customer count, total balance, and account counts by status.
4. **Cleanup.** Deletes a customer's account on `customer.deleted`, or on saga rollback (`account-rollback`).

## Code layout

```
controller/  AccountController, DashboardController
service/impl/AccountService
service/webclient/CustomerWebClientService   (customer count, customer-by-user, via gateway)
service/feignclient/UserServiceClient        (username lookup for audit logs)
events/      AccountEventConsumer (user.created, customer.deleted), AccountEventProducer,
             AccountRollbackConsumer (account-rollback)
config/      KafkaConsumerConfig, KafkaErrorHandlerConfig (3 retries × 5 s, then <topic>.DLT)
```

## Data model

`Account` (table `account_schema.account`):

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | primary key |
| `customerNo` | Long | customer number from customer-service |
| `customerId` | UUID | customer id from customer-service |
| `accNo` | Long | internal running number from sequence `account_schema.account_no_seq` |
| `accountNumber` | String | 11-digit account number, unique |
| `accountType` | enum | `SAVINGS`, `CURRENT`, `FIXED_DEPOSIT`, `RECURRING_DEPOSIT`, `NRI` |
| `balance` | BigDecimal | ≥ 0, 2 decimals |
| `currency` | `Currency` | `{ id, currencyCode }` |
| `status` | enum | `ACTIVE`, `CLOSED` |
| `isActive` | boolean | soft-delete flag |
| `createdAt`, `updatedAt` | timestamp | |

One customer can have **one account per account type** (`customerNo` + `accountType` is unique).

## API reference

All endpoints require **Permission** (JWT + role permission at the gateway).

### Accounts – `/api/v1/account`

| Method | Path | Summary |
|---|---|---|
| POST | `/create-account` | Create an account |
| PUT | `/update-account/{id}` | Update type, balance, currency, status |
| GET | `/accounts/{id}` | Account by account id |
| GET | `/get-account-by-customer-id/{customerId}` | Account summary for a customer (used by customer-service) |
| GET | `/get-account-by-customer-id-details/{customerId}` | Full account for a customer |
| DELETE | `/delete-account/{id}` | Soft delete (`isActive = false`) |
| GET | `/fetch-all-accounts` | Paginated active accounts |
| POST | `/filter-accounts` | Search accounts (filters in the body) |
| GET | `/fetch-all-currency` | Currency list |
| GET | `/account-types` | `AccountType` values |
| GET | `/account-status` | `Status` values |
| DELETE | `/customer-delete/{customerId}` | Internal: hard-delete a customer's account |

#### POST `/api/v1/account/create-account`
```json
{
  "customerNo": 1005,
  "customerId": "3f2c…",
  "accountNumber": "12345678901",
  "accountType": "SAVINGS",
  "balance": 5000.00,
  "currency": "INR"
}
```
| Field | Rules |
|---|---|
| `customerNo` | required |
| `accountNumber` | required, exactly 11 digits, unique |
| `accountType` | required, one of the `AccountType` values |
| `balance` | required, ≥ 0.00, max 10 integer digits + 2 decimals |
| `currency` | required, currency code |

Errors: account number exists; the customer already has an account of this type. Response `data`: the `Account`.

#### PUT `/api/v1/account/update-account/{id}`
All fields optional:
```json
{ "accountType": "CURRENT", "balance": 0.00, "currency": "USD", "status": "CLOSED" }
```
- An unknown currency code → `Invalid currency code`.
- You can't set `status: CLOSED` with a non-zero `balance` in the same request.
- To let customer-service delete a customer, set the balance to `0.00` and `status` to `CLOSED`.

#### GET `/api/v1/account/accounts/{id}`
Active account by account id. 404 if missing.

#### GET `/api/v1/account/get-account-by-customer-id/{customerId}`
Response `data` (`AccountsReturnDto`):
```json
{ "id": "…", "customerNo": 1005, "accNo": 17, "accountNumber": "12345678901",
  "accountType": "SAVINGS", "balance": 1000.00, "customerId": "3f2c…",
  "isActive": true, "status": "ACTIVE" }
```

#### GET `/api/v1/account/get-account-by-customer-id-details/{customerId}`
The full `Account` entity for the customer.

#### DELETE `/api/v1/account/delete-account/{id}`
Soft delete: sets `isActive = false`. Response `data`: the account.

#### GET `/api/v1/account/fetch-all-accounts`
Query: `page=0`, `size=10`, `sortField=createdAt`, `sortDirection=DESC`.
Response `data`: `{ "account": [Account…], "currentPage", "totalPages", "totalItems", "pageSize" }`

#### POST `/api/v1/account/filter-accounts`
Filters go in the **body** (all optional):
```json
{ "accountNumber": "123", "accountType": "SAVINGS", "currencyId": 1,
  "customerNo": 1005, "status": "ACTIVE",
  "page": 0, "size": 10, "sortField": "accNo", "sortDirection": "DESC" }
```

#### GET `/api/v1/account/fetch-all-currency`
`data`: `[{ "id": 1, "currencyCode": "INR" }, …]`, sorted by code.

#### GET `/api/v1/account/account-types` · GET `/api/v1/account/account-status`
`data`: `["SAVINGS","CURRENT","FIXED_DEPOSIT","RECURRING_DEPOSIT","NRI"]` · `["ACTIVE","CLOSED"]`

#### DELETE `/api/v1/account/customer-delete/{customerId}` (Internal)
Hard-deletes the customer's account. Returns 204. Called by customer-service `DELETE /email/{email}`.

### Dashboard – `/api/v1/dashboard`

| Method | Path | Summary |
|---|---|---|
| GET | `/admin` | Totals for the admin home page |
| GET | `/customer/{userId}` | The logged-in customer's account |

#### GET `/api/v1/dashboard/admin`
Calls customer-service `/api/v1/customers/users-count` with the caller's token, then aggregates accounts.
```json
{ "customerCount": 42, "totalAmount": 125000.00, "totalAccounts": 45,
  "totalClosedAccounts": 3, "activeAccounts": 40, "inactiveAccounts": 2 }
```
The caller's role also needs permission for `/api/v1/customers/users-count`, because that call goes through the gateway with the same token.

#### GET `/api/v1/dashboard/customer/{userId}`
Takes the **user id** (from login), looks up the customer via customer-service `get-customer-by-user-id`, and returns that customer's `Account`.

## Kafka

| Direction | Topic | Payload | What happens |
|---|---|---|---|
| Consumes | `user.created` (group `account-service-group`) | read as `CustomerCreatedEvent` | Creates a `SAVINGS` account, balance 1000.00, currency INR → publishes `account.created` (or `account.creation.failed`). Idempotent via `consumed_events`. |
| Consumes | `customer.deleted` | `CustomerDeletedEvent` | Deletes the customer's account |
| Consumes | `account-rollback` | `AccountRollbackEvent` (JSON string) | Saga compensation: deletes the account by `customerNo` |
| Produces | `account.created` | `AccountCreatedEvent` | Saga step done |
| Produces | `account.creation.failed` | `AccountCreationFailedEvent` | Saga fails → customer and user rolled back |

Failed messages are retried 3 times, 5 s apart, then published to `<topic>.DLT` (`KafkaErrorHandlerConfig`).

## Configuration

| Env var | Property | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | `spring.datasource.*` | PostgreSQL |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | JWT issuer | |
| `KEYCLOAK_*` | `keycloak.*` | Present in config |
| `SERVICE_URL` | `service.url` | Gateway URL for customer-service calls (default `http://gateway-service:8081`) |
| `MAIN_SERVICE_URL` | `main.service.url` | Gateway URL for Feign clients |

Resilience4j instance `accountService`: 3 retries, circuit breaker 50% over 10 calls, 10 s time limit.
