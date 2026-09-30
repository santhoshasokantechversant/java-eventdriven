# user-service

| | |
|---|---|
| **Port** | 8082 (call it through the gateway on 8081) |
| **Source** | [`user-service/`](../../user-service) |
| **DB schema** | `user_schema` |
| **Talks to** | Keycloak (admin API + token endpoint), customer-service (via gateway), Kafka |
| **Owns** | Users, login/tokens, roles and role hierarchy, privileges (menu items), endpoints, permissions |

## What it does

1. **Authentication.** Logs users in by exchanging email and password for Keycloak tokens (password grant), refreshes tokens, and logs out.
2. **User management.** Creates users in Keycloak *and* the local `users` table, updates and deletes them, and assigns roles.
3. **Onboarding.** Emails new users a sign-up link. The user sets a password with `register-new`, and uses forgot / reset password to recover access.
4. **RBAC (role-based access control)** – roles form a hierarchy (`position` = `1`, `1.1`, `1.2`, `2`, …). Each role has a *permission* row per (privilege × endpoint). Permissions are copied into the Keycloak role's `permissions` attribute, which is what the **gateway** reads to allow/deny each request (see [Security & Keycloak](../security-keycloak.md)).
5. **Sidebar menu.** Login returns the menu (`sidenav`) the role is allowed to see.
6. **Saga participant.** Consumes `customer.created` to create the customer's login user, and publishes `user.created` / `user.creation.failed`. See [Kafka events & saga](../kafka-events-and-saga.md).

## Code layout

```
controller/   UserController, RoleController, PrivilegeAndPermissionController (v1),
              PrivilegesController, EndpointsController, PermissionController (v2)
service/impl/ UserService, RoleService, PrivilageService, PermissionService, EndponitService
service/keyclock/ KeyclockUserService (users, login, tokens), KeyclockRoleService (roles + permission attributes)
consumer/     UserEventConsumer (customer.created/updated/deleted), UserRollbackConsumer (rollbacks)
producer/     UserEventProducer (user.created, user.creation.failed)
utils/services/ EmailService, HtmlTemplateUtil, AESUtil (encrypts reset-password links)
resources/templates/ UserCreation.html, forgetPassword.html
```

## Key concepts

| Concept | Table | Meaning |
|---|---|---|
| **User** | `users` | Local copy of a Keycloak user. `userType` = `USER` (staff) or `CUSTOMER`. `registered` = `YES` once the user has set a password. `staticUser = YES` marks the built-in admin. |
| **Role** | `roles` | Mirrors a Keycloak realm role. `position` places it in the hierarchy (`1` = Admin). `roleCreate = YES` lets holders create roles. |
| **Privilege** (v2) | `privileges_new` | A menu item / module (name, slug, icon, position, optional parent). |
| **Endpoint** (v2) | `endpoints` | A backend API (`backendUrl` + `httpMethod`) attached to a privilege. |
| **Permission** (v2) | `permission_new` | One row per role × endpoint: `permissionType` (YES/NO = may call it), `sideNav` (YES/NO = show in menu). |
| v1 tables | `privileges`, `permission`, `side_nav`, `privilege_endpoint` | Older model, still served by `/api/v1/privileges-permissions`. |

## API reference

Legend: **Auth** = `Public` (no token), `Token` (valid JWT, no permission check), `Permission` (JWT + the role must have permission for this endpoint), `Internal` (called by other services, still through the gateway with the user's token).

### Users – `/api/v1/users`

| Method | Path | Auth | Summary |
|---|---|---|---|
| POST | `/create-admin` | Public | One-time bootstrap of the Admin role + admin user |
| POST | `/login` | Public | Log in, get tokens + profile + menu |
| POST | `/refresh-token/{refreshToken}` | Public | Get a new access token |
| POST | `/logout?refreshToken=` | Token | Revoke the refresh token |
| POST | `/register-new` | Public | First-time password setup for an invited user |
| POST | `/forgot-password-mail-send` | Public | Email a reset-password link |
| GET | `/get-by-id-encrypted/{id}` | Public | Validate a reset link, return who it belongs to |
| POST | `/reset-password` | Public | Set a new password from a reset link |
| POST | `/add-user` | Permission | Create a staff user |
| GET | `/get-all-users` | Permission | Paginated list of active users |
| GET | `/get-user-by-id/{id}` | Permission | One user |
| GET | `/filter-users` | Permission | Search users |
| PUT | `/update-user/{id}` | Permission | Update profile, role, active status |
| PUT | `/update-new-password/{id}` | Permission | Change password (needs old password) |
| PUT | `/update-user-role/{id}` | Permission | Change a user's role |
| DELETE | `/delete-user/{id}` | Permission | Delete a user |
| GET | `/users-count` | Permission | Count of active users |
| POST | `/check-exist-customer` | Internal | Is this email/phone free? (used by customer-service) |
| DELETE | `/customer-delete/{id}` | Internal | Delete a user by id (customer deletion) |
| GET | `/find-username-by-username/{username}` | Internal | Resolve username (plain text) |
| GET | `/find-username-by-email/{email}` | Internal | Resolve username from email (plain text) |

#### POST `/api/v1/users/create-admin`
Creates the `Admin` role (position `1`, can create roles) if missing, then the static admin user, and seeds four generic endpoints (`get`, `post`, `put`, `delete`). It can only succeed once (a second call fails because a static user already exists).

- **Body:** none
- **Creates:** email `admin@gmail.com`, username like `admina1234`, password = the `ADMIN_INITIAL_PASSWORD` environment variable (the call fails if it isn't set)
- **Response `data`:** the created `User`
- ⚠️ This endpoint is public. Call it once right after deployment, then change the password.

#### POST `/api/v1/users/login`
Checks that the email belongs to an active, **registered** user, exchanges the credentials with Keycloak, decodes the token, and adds the role's sidebar menu.

Request:
```json
{ "email": "admin@gmail.com", "passwordHash": "Admin@123" }
```
| Field | Rules |
|---|---|
| `email` | required, valid email |
| `passwordHash` | required, min 8 chars (the plain password; the name is historical) |

Response `data` (`LoginReturnDto`):
```json
{
  "token": { "accessToken": "eyJ...", "refreshToken": "eyJ...", "valid": "ok", "message": null },
  "userName": "admina1234",
  "fullName": "Admin A",
  "emailId": "admin@gmail.com",
  "id": "5b0c…",
  "role": "Admin",
  "roleId": "9e1f…",
  "sidenav": [
    { "id": "…", "moduleName": "Customers", "slugName": "customers", "icon": "users", "position": 1,
      "subPrivileges": [], "access": ["get", "post"] }
  ]
}
```
Errors: unknown email → 404 `Email Id doesn't match.`; not registered → `User not registered yet.`; wrong password → `Password is Incorrect`.

#### POST `/api/v1/users/refresh-token/{refreshToken}`
Exchanges a refresh token for a new token pair. Response `data`: `TokenDto` (`accessToken`, `refreshToken`, `valid`, `message`). An expired/invalid token gives an error.

#### POST `/api/v1/users/logout?refreshToken=<token>`
Calls Keycloak's logout endpoint to revoke the session. Response `data`: `true`. Needs a valid access token at the gateway.

#### POST `/api/v1/users/register-new`
Completes onboarding: a user created by an admin (or by the customer saga) receives an email with a sign-up link, then sets a password here. Sets `registered = YES`.

```json
{ "emailId": "john@example.com", "password": "Secret@123", "confirmPassword": "Secret@123" }
```
Errors: unknown email, passwords don't match, already registered.

#### POST `/api/v1/users/forgot-password-mail-send`
Accepts an email **or username** in the `email` field. Encrypts the user id with AES-GCM (random IV, so every link is unique), stores a `reset_password` row valid for **2 minutes**, and emails `FORGOT_PASSWORD_FRONTEND_URL + <encryptedId>`.

```json
{ "email": "john@example.com" }
```
Response: `{"status":"success","message":"Reset password mail sent."}`

#### GET `/api/v1/users/get-by-id-encrypted/{id}`
The frontend calls this when the reset link is opened. Validates the link (exists, `ACTIVE`/`VIEWED`, not expired), marks it `VIEWED`, and returns `{ "email", "userName", "id" }`.

#### POST `/api/v1/users/reset-password`
```json
{ "encryptedUserId": "<id from the link>", "email": "john@example.com",
  "password": "New@1234", "confirmPassword": "New@1234" }
```
Validates the link again, sets the password in Keycloak, marks the link `COMPLETED`.

#### POST `/api/v1/users/add-user`
Creates a staff user in Keycloak (with realm role) and in `users` + `user_roles`. If the role is `Customer`, the user type becomes `CUSTOMER` and the onboarding email is sent.

```json
{
  "email": "jane@bank.com", "firstName": "Jane", "lastName": "Doe",
  "roleId": "9e1f…", "phoneNumber": "9876543210",
  "userName": "optional – generated if empty",
  "passwordHash": "optional – else set via register-new",
  "dateOfBirth": "1990-05-01", "address": "…", "city": "…", "state": "…",
  "postalCode": "…", "country": "…"
}
```
| Field | Rules |
|---|---|
| `email` | required, valid, unique among active users |
| `firstName`, `lastName` | required, max 50 |
| `roleId` | required, UUID of an active role |
| `phoneNumber` | unique among active users |
| `userName` | optional; generated as `first name + first 3 letters of last name + 4 digits` |

Response `data`: the `User`. Errors: username/email exists → 302, phone exists → 404 (see known issues).

#### GET `/api/v1/users/get-all-users`
Query: `page=0`, `size=10`, `sortField=createdAt`, `sortDirection=DESC`. Response `data`: paginated `UserDisplayDto` list.

`UserDisplayDto`: `id, userName, email, firstName, lastName, createdAt, updatedAt, userNo, dateOfBirth, address, city, state, postalCode, country, phoneNumber, isActive, roleId`

#### GET `/api/v1/users/get-user-by-id/{id}`
Response `data`: `UserDisplayDto`. 404 if not found or inactive.

#### GET `/api/v1/users/filter-users`
Query parameters (all optional, partial and case-insensitive match): `firstName`, `lastName`, `userName`, `email`, `roleId`, plus `page`, `size`, `sortField` (default `createdAt`), `sortDirection` (default `DESC`).

#### PUT `/api/v1/users/update-user/{id}`
```json
{ "firstName": "Jane", "lastName": "Doe", "roleId": "9e1f…", "status": "active",
  "dateOfBirth": "1990-05-01", "address": "…", "city": "…", "state": "…",
  "postalCode": "…", "country": "…" }
```
Updates the local user and the Keycloak user (name, email, username, adds the role). `status` = `active`/`inactive` toggles `isActive`. `email` and `phoneNumber` in the DTO are ignored by the update. `roleId` is **required** (it's parsed unconditionally).

#### PUT `/api/v1/users/update-new-password/{id}`
```json
{ "oldPassword": "Old@1234", "newPassword": "New@1234", "confirmNewPassword": "New@1234" }
```
Verifies `oldPassword` by logging in to Keycloak, then sets the new password. `confirmNewPassword` is not checked server-side.

#### PUT `/api/v1/users/update-user-role/{id}`
```json
{ "roleId": "9e1f…", "roleName": "Manager" }
```
Replaces all the user's Keycloak realm roles with this one and updates `user_roles`.

#### DELETE `/api/v1/users/delete-user/{id}`
- Staff user: deleted from Keycloak and the database.
- `CUSTOMER` user: first asks customer-service (`/api/v1/customers/delete-customer-by-user-id/{id}`) to delete the customer. That call refuses if the customer's account is still active or has a non-zero balance. The user is deleted only if it succeeds.

#### GET `/api/v1/users/users-count`
Response: `data` and `count` = number of active users.

#### Internal endpoints
- `POST /check-exist-customer`: body `{ "email", "phoneNumber" }`. `data: true` if neither is used by an active user, otherwise `status: error`, `data: false`.
- `DELETE /customer-delete/{id}`: deletes the user (Keycloak + DB). Returns 204.
- `GET /find-username-by-username/{username}`, `GET /find-username-by-email/{email}`: return the username as **plain text** (not the JSON envelope), or empty. Used by account/customer services for audit logging.

### Roles – `/api/v1/roles`

| Method | Path | Summary |
|---|---|---|
| POST | `/add` | Create a role (Keycloak + DB) and copy permissions from its parent |
| GET | `/get-all-roles` | Paginated active roles (`page`, `size`, `sortField=createdAt`, `sortDirection=DESC`) |
| GET | `/get-role-by-id/{id}` | One role |
| GET | `/filter-roles` | Roles visible to the caller, filtered by `name` |
| PUT | `/update-role/{id}` | Update name, description, `roleCreate` |
| DELETE | `/delete-role/{id}` | Delete (fails if any user has this role) |
| GET | `/fetch-all-roles` | All active roles, no paging |
| GET | `/dropdown-list` | Roles the caller may assign (below them in the hierarchy) |

All require **Permission**.

#### POST `/api/v1/roles/add`
```json
{ "name": "Branch Manager", "description": "Manages a branch", "hierarchy": "<optional role id>" }
```
| Field | Rules |
|---|---|
| `name` | required, 3–50 chars, unique among active roles |
| `hierarchy` | optional. If set, the new role takes that role's position, and existing roles at or after it shift down. If empty, the role goes under the caller (`1.x` for a sub-role) or at the next root position if the caller is Admin. |

Steps: check the caller (from `X-User-Roles`) has `roleCreate = YES` → compute the position → create a Keycloak realm role → save → copy the parent role's permission rows (all `NO`, or all `YES` for position `1`) → write them to the Keycloak role attributes.

#### GET `/api/v1/roles/filter-roles`
Query: `name`, `page`, `size`, `sortField`, `sortDirection`. Returns only roles **below** the caller in the hierarchy (Admin sees all), sorted by hierarchy position.

#### PUT `/api/v1/roles/update-role/{id}`
```json
{ "name": "Senior Manager", "description": "…", "roleCreate": "YES" }
```
`name` and `description` must both be present (they're checked with `isBlank()`). Also renames the Keycloak role.

#### DELETE `/api/v1/roles/delete-role/{id}`
Fails with 500 `Deletion failed, <role> is already assigned to the user` if any user holds it. Otherwise deletes the Keycloak role, its permission rows, and the role.

### Privileges (v2) – `/api/v2/privileges`

Privileges are menu items/modules. Creating one generates permission rows for every role × generic endpoint.

| Method | Path | Summary |
|---|---|---|
| POST | `/create` | Create a privilege (+ side-nav entry + permissions for every role) |
| PUT | `/update/{id}` | Update it |
| DELETE | `/delete/{id}` | Delete it with its permissions and side-nav rows |
| GET | `/{id}` | One privilege |
| GET | `/filter-list` | Paginated, filter by `name` (`page`, `size`, `sortField`, `sortDirection`) |
| GET | `/list` | All privileges |

Body for create/update (`PrivilegesDto`):
```json
{ "name": "Customers", "description": "Customer module", "slugName": "customers",
  "icon": "users", "position": 2, "url": "/api/v1/customers", "subName": "<parent privilege id or empty>" }
```

### Endpoints (v2) – `/api/v2/privileges/endpoints`

An endpoint is one backend API (URL + method) that belongs to a privilege. Creating one adds a permission row for every role (Admin = `YES`, others = `NO`) and pushes permissions to Keycloak.

| Method | Path | Summary |
|---|---|---|
| POST | `/create` | Create an endpoint |
| PUT | `/update/{id}` | Update it and the related permission rows |
| DELETE | `/delete/{id}` | Delete it and its permission rows (generic seed endpoints can't be deleted) |
| GET | `/{id}` | One endpoint |
| GET | `/filter-list` | Paginated, filter by `name` |
| GET | `/list-all` | All active endpoints |

```json
{ "endpointName": "Create customer", "backendUrl": "/api/v1/customers/create-customer",
  "httpMethod": "post", "privilegeId": "<privilege id>" }
```
Duplicate (same name + method) → error `Endpoint already exist with same method`.

> The gateway grants access when the request path **contains** `backendUrl` (case-insensitive) and the method matches. Use specific URLs: `/api/v1/customers` would match every customer API.

### Permissions (v2) – `/api/v2/privileges/permission`

| Method | Path | Summary |
|---|---|---|
| GET | `/list-by-roles/{roleId}` | A role's permissions grouped by privilege, each marked `updatable` YES/NO for the caller |
| PUT | `/save-roles/{roleId}` | Bulk set YES/NO per permission row → syncs to Keycloak |
| PUT | `/update/{id}` | Change a privilege's slug / side-nav flag for that role |
| GET | `/get-permission/{id}` | One permission row |

`PUT /save-roles/{roleId}` body:
```json
[ { "id": "<permission row id>", "permissionType": "YES" },
  { "id": "<permission row id>", "permissionType": "NO" } ]
```

`GET /list-by-roles/{roleId}` response `data`:
```json
[ { "key": "Customers", "privilegeId": "…",
    "values": [ { "id": "…", "endpointName": "Create customer", "backendUrl": "/api/v1/customers/create-customer",
                  "httpMethod": "post", "permissionType": "YES", "sideNav": "YES", "updatable": "YES" } ] } ]
```
`updatable` is `YES` only if the caller is Admin, or is above the role in the hierarchy **and** holds that permission themselves. You can't edit your own role.

### Privileges & permissions (v1, legacy) – `/api/v1/privileges-permissions`

The older RBAC model. The frontend login menu now uses the v2 model (`sideNavNew`). Prefer the v2 APIs for new work.

| Method | Path | Summary |
|---|---|---|
| POST | `/add-privileges` | Create a v1 privilege (+ side-nav, endpoints, permissions for all roles) |
| POST | `/add-endpoints` | Create a v1 endpoint for a privilege |
| POST | `/add-permission` | Create one permission (role + privilege + endpoint, optional user) |
| PUT | `/update-permission/{roleId}` | Replace a role's permissions (list of `Permission`) → Keycloak |
| GET | `/list-permission/{roleId}` | A role's v1 permissions |
| GET | `/side-nav/{roleId}` | v1 side-nav for a role |
| GET | `/list-side-nav` | Full v1 side-nav tree |
| PUT | `/update-slug/{id}` | Update one permission's slug |
| GET | `/get-permission/{id}` | One v1 permission |

## Kafka

| Direction | Topic | Payload | What happens |
|---|---|---|---|
| Consumes | `customer.created` | `CustomerCreatedEvent` | Creates a `CUSTOMER` user with role `Customer` (username `firstname+lastname+customerNo`), emails a sign-up link, publishes `user.created`. On failure publishes `user.creation.failed`. Idempotent via `consumed_events`. |
| Consumes | `customer.updated` | `CustomerUpdatedEvent` | Finds the user by email and updates profile fields |
| Consumes | `customer.deleted` | `CustomerDeletedEvent` | Deletes the user (Keycloak + DB) |
| Consumes | `customer.rollback` | `UserRollbackEvent` | Saga compensation: deletes the user by email (Keycloak + DB), 3 retries |
| Consumes | `account.creation.failed` | `AccountCreationFailedEvent` | Saga compensation: deletes the user created for that customer |
| Produces | `user.created` | `UserCreatedEvent` | → account-service creates the account; customer-service marks the saga step done |
| Produces | `user.creation.failed` | `UserCreationFailedEvent` | → customer-service fails the saga |

Details: [Kafka events & saga](../kafka-events-and-saga.md).

## Configuration

| Env var | Property | Purpose |
|---|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | `spring.datasource.*` | PostgreSQL |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | JWT issuer | e.g. `http://keycloak:8080/realms/bank_system` |
| `KEYCLOAK_SERVER_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET` | `keycloak.*` | Client used for login / token calls |
| `KEYCLOAK_ADMIN_USERNAME`, `KEYCLOAK_ADMIN_PASSWORD` | `keycloak.admin.*` | Master-realm admin for managing users/roles |
| `SMTP_MAIL_USERNAME`, `SMTP_MAIL_PASSWORD` | `spring.mail.*` | Gmail SMTP |
| `FORGOT_PASSWORD_FRONTEND_URL` | `service.forget-password-url` | Base of the reset link |
| `REGISTER_FRONTEND_URL` | `service.register-url` | Link in the welcome email |
| `ENCRYPTION_SECRET_KEY` | `service.encryption-secret-key` | AES key for reset links (16/24/32 chars) |
| `ADMIN_INITIAL_PASSWORD` | `service.admin-initial-password` | Password for the admin created by `create-admin` |
| `SERVICE_URL` | `service.url` | Gateway URL for calls to customer-service (default `http://gateway-service:8081`) |
| `MAIN_SERVICE_URL` | `main.service.url` | Gateway URL for Feign clients |

Kafka bootstrap is `kafka:9092` (in `application.yml`); consumer group `user-service-group`.

## Email templates

| Template | Sent when | Placeholders |
|---|---|---|
| `UserCreation.html` | `add-user` for a Customer-role user; customer saga | name, userName, email, phoneNumber, role, link |
| `forgetPassword.html` | `forgot-password-mail-send` | name, link |
