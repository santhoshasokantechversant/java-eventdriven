# Known issues

Found during the code review (September 2026). Ordered roughly by impact. Items marked ✅ were fixed in the same change that added this documentation.

## Bugs

### 1. Flyway migration never runs
`customer-service/src/main/resources/db.migrations/V1__sequenceadd.sql` sits in `db.migrations/`, but Flyway only scans `db/migration/`. The `customer_no_seq` sequence is never created by the app, and `users_no_seq` / `account_no_seq` have no script at all.
**Fix:** move the file to `src/main/resources/db/migration/`, and add similar migrations in user-service and account-service for their sequences. Until then, create them by hand ([Getting started](getting-started.md#2-database)).

### 2. ✅ Circuit-breaker fallbacks didn't match their methods
Resilience4j only uses a fallback whose parameters are the original method's parameters plus a `Throwable`. None of the four customer fallbacks matched, so an open circuit produced a "no fallback method" error instead of a clean 503.
**Fixed:** signatures aligned in `CustomerController` and `CustomerService`. The service fallbacks rethrow business errors unchanged and report "service unavailable" only when the circuit is open (`CallNotPermittedException`). The unused `deleteCustomerFallback` methods and `CommonFallbackHandler` were removed.

### 3. Error status overwritten with success
`UserController.getUserByEncrypted` and `UserController.resetPassword` set `status = error`, then set `status = success` on the next line with no `return` in between. (The service throws for real failures, so the practical effect is limited, but the branch is dead and misleading.)
**Fix:** `return` after setting the error, or remove the branch.

### 4. A delete endpoint uses GET
`GET /api/v1/customers/delete-customer-by-user-id/{id}` deletes a customer. Caches, prefetchers and crawlers may call GETs freely.
**Fix:** change it to `DELETE`, and update user-service's `CustomerServiceWebClient`.

### 5. `update-customer` fails if `status` is missing
`CustomerService.updateCustomer` calls `customerDTO.getStatus().equals("ACTIVE")`, which throws a NullPointerException when `status` is absent. Any value other than `ACTIVE` (including `BLOCKED`) sets `INACTIVE`.
**Fix:** null-check it and map `status` with `Status.valueOf`.

### 6. user-service returns 302 for business errors
`GlobalExceptionHandler` in user-service maps `EmailAlreadyExistsException`, `UserNameAlreadyExistsException`, `RoleNameAlreadyExistException` and `SomethingWentWrongException` to **302 FOUND** (a redirect status), and `PhoneNumberAlreadyExistException` to 404. Browsers and HTTP clients may try to follow the "redirect".
**Fix:** use 409 CONFLICT for duplicates and 400 or 422 for business rule failures.

### 7. Permission denied returns 500 instead of 403
The gateway's `PermissionFilter` fails with `SomethingWentWrongExceptions`, which reaches the client as HTTP 500. Clients can't tell "not allowed" from "server broken".
**Fix:** in `PermissionFilter`, set `exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)` and write the JSON body, as `SecurityConfig.customAccessDeniedHandler` already does.

### 8. Possible race after customer creation
After the saga succeeds, `CustomerController` calls `updateUserId(...)` asynchronously. It reads the customer ↔ user link that `CustomerEventConsumer` stores from `user.created` in a **different** consumer group. If that consumer hasn't processed the event yet, `findByCustomerId` returns null and the welcome email isn't sent.
**Fix:** store the link from the saga consumer (same group that completes the saga), or retry.

### 9. user-service `application-local.yml` is mis-nested
Its settings are indented under a second `spring:` key (`spring.spring.datasource…`), so the `local` profile doesn't configure anything.
**Fix:** remove the extra nesting level.

### 10. Audit log loses the entity
The `AUDIT_FILE` JSON pattern in every `logback-spring.xml` hardcodes `"entity": "Security"`.
**Fix:** `"entity": "%X{entity:-unknown}"`.

### 11. Staff users get no welcome email
`UserService.addUser` only emails `CUSTOMER`-type users. Staff created via `add-user` without a password have no way to learn their sign-up link.
**Fix:** in `addUser`, call `selfProxy.sendEmail(user, role.getName())` for staff users too (through `selfProxy` so `@Async` applies).

## Security

| # | Issue | Status |
|---|---|---|
| S1 | Credentials committed to git (compose files, `.env`, old `k8s1/`, configserver GitLab token) | ✅ removed from `k8s/` and configserver. **Still in the compose files and `.env`.** All values are in git history: **rotate them.** |
| S2 | Backends trust the `X-Gateway-Auth` header only | ✅ mitigated in K8s (backends are ClusterIP). Still exposed on ports 8082–8084 in Docker Compose. |
| S3 | Public `create-admin` with a hardcoded password | ✅ password now from `ADMIN_INITIAL_PASSWORD`; endpoint is still public |
| S6 | Reset-password links encrypted with AES/ECB (deterministic, no IV) | ✅ AES-GCM with a random IV |
| S4 | Admin bypass by role name, substring permission matching, no caching | open. See [Security](security-keycloak.md#security-findings-to-fix) |
| S5 | TRACE logging of SQL bind parameters | open |

## Architecture gaps

| Gap | Impact | Suggestion |
|---|---|---|
| Saga state is in memory (customer-service tracker, user-service email map) | Lost on restart; breaks with more than 1 replica | Keep a saga table in the DB (or Redis) keyed by `correlationId`; run 1 replica until then |
| Create-customer blocks the HTTP request up to 30 s | Ties up threads; slow UX | Return 202 + poll or push status |
| Only create consumers are idempotent | Redelivered update/delete events are re-applied | Use `consumed_events` in every consumer |
| Config server deployed but unused | Confusing; config duplicated per service | Adopt it ([Config server](services/config-server.md)) or remove it |
| Logs not shipped to ELK | Kibana empty | Add `LogstashTcpSocketAppender` ([Logging](logging-monitoring.md)) |
| No distributed tracing | Hard to debug cross-service flows | Micrometer Tracing + OpenTelemetry |
| No tests beyond context-load; CI skips tests | Regressions go unnoticed | Unit + Testcontainers tests; run in CI |
| No OpenAPI/Swagger | These docs are the only API reference | Add `springdoc-openapi-starter-webmvc-ui` to each service |
| `ddl-auto: update` alongside Flyway | Schema drift; Flyway never owns the schema | Move to Flyway migrations, set `ddl-auto: validate` |
| Hardcoded IPs (`10.1.0.47`) in `application.yml` and CORS config | Breaks outside that network | Env vars (done for K8s via ConfigMap) |
| v1 and v2 permission models both live | Two sources of truth | Retire `/api/v1/privileges-permissions` once the frontend no longer uses it |
| Kafka single broker, replication factor 1 | No fault tolerance | 3 brokers + RF 3 for anything beyond dev |

## Cleanup done in this change ✅

- Removed unused files: `KafkaAccountDto`, `FailureEventsConsumer` (listeners were commented out), `SubRegionRepository`, `PrivilageEndpointDto`, three empty `*MapperNew` components, root `index.js`, `common-lib/Dockerfile`, an empty `common-lib` `application.properties`, and a stray build output `eureka-server/${project.build.directory}/…`.
- Removed commented-out code blocks, `System.out.println` debug output (replaced with SLF4J where the message was useful), unused and duplicate imports, and two dead private methods in `RoleService`.
- Removed two gateway routes (`/api/v2/endpoints/**`, `/api/v2/permission/**`) that no controller served.
- Stopped tracking `.idea/`; added `.idea/`, `*.iml`, `target/`, `k8s/00-config/secrets.yaml` to `.gitignore`.
- Replaced `k8s/` and `k8s1/` with one corrected `k8s/` folder ([Kubernetes](kubernetes.md#what-changed-from-the-old-k8s-and-k8s1-folders)).
- Replaced the GitLab token in configserver with `CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD`.

## Static-analysis fixes (Sonar rules) ✅

Checked with PMD and SpotBugs (Max effort) plus a scan for common SonarQube rules. All modules build cleanly afterwards.

| Rule | Fix |
|---|---|
| S5542 weak cipher mode | `AESUtil`: AES/ECB → AES/GCM with a random 12-byte IV, UTF-8 |
| S2245 insecure `Random` | Username suffix uses `SecureRandom` (the old time-seeded `Random` repeated within the same millisecond) |
| S2068 hardcoded password | `create-admin` reads `ADMIN_INITIAL_PASSWORD` |
| S2259 null dereference | `CustomerService.deleteCustomerByUserId` (customer and account response used before null checks); `UserController.deleteUser` (`getBody()`) |
| S108 empty catch | `UserService.createAdmin` swallowed every error and returned `null` (the caller then threw an NPE); it now logs and rethrows |
| S2142 interrupt swallowed | Kafka `send().get()` calls restore the interrupt flag |
| S6809 `@Async` self-invocation | `addUser` called `this.sendEmail`, so it ran synchronously; now called through `selfProxy` |
| S2387 field shadowing | `AccountRollbackEvent`, `UserRollbackEvent`, `CustomerDeletedEvent` no longer re-declare `EventBase` fields |
| S112 / S2221 generic exceptions | No production code throws `RuntimeException` or catches `Exception` any more; checked exceptions are caught by type |
| S1148 `printStackTrace` | Replaced with SLF4J logging |
| S1068 / S1144 unused code | Unused `gson` field, `SagaCompletionEvent`, `UserUpdatedEvent`, both `HtmlTemplateUtil` classes, `CommonFallbackHandler`, dead private methods |
| S3749 / S1312 | Injected field made `final`; logger made `private static final` |
| S2333 / S1110 / S1116 | Redundant `public` on repository methods, useless parentheses, stray semicolon, redundant `Constants.` qualifiers |
| Default charset | JWT payload decoding uses UTF-8 |
| Lombok `@Builder` defaults | `Account` defaults (`balance`, `isActive`, `status`) marked `@Builder.Default` |
| Maven duplicate dependencies | Removed from all five `pom.xml` files |

Not changed on purpose: SpotBugs `EI_EXPOSE_REP*` (fires on every Spring-injected bean and Lombok DTO; Sonar has no equivalent), `CT_CONSTRUCTOR_THROW` on utility classes (a throwing private constructor is the recommended pattern), duplicated string literals (S1192) and cognitive complexity (S3776), which need refactoring beyond a mechanical fix.
