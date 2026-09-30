# common-lib

| | |
|---|---|
| **Type** | Plain Maven jar (`com.techversant:common-lib:0.0.1-SNAPSHOT`), **not a running service** |
| **Source** | [`common-lib/`](../../common-lib) |
| **Used by** | gateway-service, user-service, account-service, customer-service |

## What it contains

### Kafka event classes – `com.techversant.common_lib.events`

Shared so producers and consumers serialize the same JSON. Most events extend `EventBase`:

```java
class EventBase { String eventId; String eventName; String timeStamp; }
```

| Class | Fields (besides `EventBase`) | Topic |
|---|---|---|
| `CustomerCreatedEvent` | customerNo, customerId, firstName, lastName, email, phoneNumber, dateOfBirth, address, city, state, postalCode, country, status, createdAt, correlationId | `customer.created` |
| `CustomerUpdatedEvent` | customerNo, firstName, lastName, email, phoneNumber, dateOfBirth, status, updatedAt, address, city, state, postalCode, country | `customer.updated` |
| `CustomerDeletedEvent` | customerId, userId, customerNo, reason | `customer.deleted` |
| `CustomerRollbackEvent` | customerNo, reason, userId | `customer.rollback` |
| `UserRollbackEvent` | email, phoneNumber, reason | `customer.rollback` |
| `UserCreatedEvent` | id, userId, userName, firstName, lastName, email, status, createdAt, customerId, customerNo, correlationId | `user.created` |
| `UserCreationFailedEvent` | customerNo, reason, errorDetails, correlationId | `user.creation.failed` |
| `AccountCreatedEvent` | customerNo, accountNo, accountType, balance, currency, status, createdAt, correlationId | `account.created` |
| `AccountCreationFailedEvent` | customerNo, reason, errorDetails, correlationId | `account.creation.failed` |
| `AccountRollbackEvent` | customerNo, reason | `account-rollback` |

Subclasses must not re-declare `eventId`, `eventName` or `timeStamp`; use the `EventBase` fields. (The rollback and delete events used to shadow them, including a second `timestamp` field; that was removed.)

### Shared DTOs

| Class | Used for |
|---|---|
| `AccountsReturnDto` | account-service → customer-service account summary |
| `CustomerReturnDto` | customer-service → user-service deleted customer |
| `UserDetailsDto` | `{ email, phoneNumber }` for `check-exist-customer` |
| `CustomerUserDto` | customer ↔ user id pair |
| `AccountType`, `Status` | shared enums |

### `AuditLogger` – `com.techversant.common_lib.utility`

```java
AuditLogger.log(user, ip, entity, action, status);
```
Puts `user`, `ip`, `entity` and `status` into the MDC and logs `action` on the logger `com.techversant.security`. Each service's `logback-spring.xml` routes that logger (and only that logger) to a JSON file, `/app/logs/<service>/audit.log`. Used by controllers for create/update/delete and by the gateway for 401/403 events. See [Logging & monitoring](../logging-monitoring.md).

## Building

Install it into your local Maven repository **before** building any service:

```bash
cd common-lib
./mvnw clean install -DskipTests      # Windows: mvnw.cmd clean install -DskipTests
```

The service Dockerfiles do this in their first build stage, so Docker builds don't need it installed locally.

## Changing an event

Consumers deserialize JSON into these classes. When you add a field, rebuild common-lib **and** every service that uses the event. Renaming or removing a field breaks consumers that are still running the old version.
