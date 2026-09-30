# Getting started

## Prerequisites

| Tool | Version | Used for |
|---|---|---|
| JDK | 17+ | building and running services |
| Maven | via wrapper (`mvnw` / `mvnw.cmd` in each module) | build |
| Docker Desktop | recent | containers, and its built-in Kubernetes |
| kubectl | matches your cluster | Kubernetes deploys |
| PostgreSQL | 15 | application database (local or managed, e.g. Aiven) |

## Repository layout

```
account-service/     customer-service/     user-service/     gateway-service/
eureka-server/       configserver/         common-lib/       (one Maven project each)
k8s/                 Kubernetes manifests + build/deploy scripts
infra/               one-line Dockerfiles that pin infra images (used by docker-compose-build.yml)
logging/             Elasticsearch / Kibana / Logstash config
kafka/               standalone Kafka compose + topic-creation script
keyclock/            standalone Keycloak compose
docker-compose*.yml  three Compose variants (see docker.md)
build-all.sh         builds every module in order (used by CI)
documents/           this documentation
```

## 1. Build

`common-lib` must be installed into your local Maven repository first, because every service depends on it.

```bash
cd common-lib && ./mvnw clean install -DskipTests && cd ..
./build-all.sh          # or build each service: cd user-service && ./mvnw clean package -DskipTests
```

On Windows PowerShell use `.\mvnw.cmd` instead of `./mvnw`.

## 2. Database

The three services share one database, `bank_management`, and create their own schemas and tables (`ddl-auto: update`). Before the first start, create the database, the schemas and the number sequences:

```sql
CREATE DATABASE bank_management;
\c bank_management
CREATE SCHEMA IF NOT EXISTS user_schema;
CREATE SCHEMA IF NOT EXISTS customer_schema;
CREATE SCHEMA IF NOT EXISTS account_schema;

CREATE SEQUENCE IF NOT EXISTS user_schema.users_no_seq        START WITH 1001;
CREATE SEQUENCE IF NOT EXISTS customer_schema.customer_no_seq START WITH 1001;
CREATE SEQUENCE IF NOT EXISTS account_schema.account_no_seq   START WITH 1001;
```

> The sequences are read with `nextval(...)` when creating users, customers and accounts. The only Flyway migration (`customer-service/src/main/resources/db.migrations/`) is in a folder Flyway doesn't scan, so nothing creates them automatically. See [Known issues](known-issues.md#1-flyway-migration-never-runs).

Location reference data (regions, countries, states, cities) and currencies must also be loaded into `customer_schema` and `account_schema`. There's no seed script in the repo.

## 3. Environment variables

Every service reads its settings from environment variables. Set these in your IDE run configuration, a local untracked `.env`, or the Kubernetes Secret/ConfigMap. Never commit real values.

| Variable | Used by | Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | user, customer, account | `jdbc:postgresql://localhost:5432/bank_management` |
| `SPRING_DATASOURCE_USERNAME` / `_PASSWORD` | user, customer, account | |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | all four | `http://keycloak:8080/realms/bank_system` |
| `KEYCLOAK_SERVER_URL` | all four | `http://keycloak:8080` |
| `KEYCLOAK_REALM` / `KEYCLOAK_CLIENT_ID` | all four | `bank_system` / `poc-api-gateway` |
| `KEYCLOAK_CLIENT_SECRET` | all four | from the Keycloak client |
| `KEYCLOAK_ADMIN_USERNAME` / `_PASSWORD` | all four | master-realm admin |
| `SMTP_MAIL_USERNAME` / `_PASSWORD` | user, customer | Gmail address + app password |
| `FORGOT_PASSWORD_FRONTEND_URL` | user | `http://localhost:2000/set-password/` |
| `REGISTER_FRONTEND_URL` | user, customer | `http://localhost:2000/signup` |
| `ENCRYPTION_SECRET_KEY` | user | AES key, 16/24/32 characters |
| `ADMIN_INITIAL_PASSWORD` | user | password for the admin created by `create-admin` |
| `USER_SERVICE` / `ACCOUNT_SERVICE` / `CUSTOMER_SERVICE` / `GATEWAY_SERVICE` | gateway | `http://user-service:8082`, … |
| `SERVICE_URL` / `MAIN_SERVICE_URL` / `USER_SERVICE_URL` | user, customer, account | gateway URL for service-to-service calls; default `http://gateway-service:8081` (set `http://localhost:8081` when running from the IDE) |
| `CORS_ALLOWED_ORIGINS` | gateway | comma-separated frontend origins; has a default |

Hostnames like `kafka` and `eureka-server` are hardcoded in `application.yml` and resolve inside Docker/Kubernetes networks. To run a service straight from the IDE against infrastructure on your machine, either:
- add `127.0.0.1 kafka keycloak eureka-server` to your hosts file (Kafka must then advertise `kafka:9092`), or
- override them with env vars: `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka` and `SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092` (user-service sets the bootstrap per client, so also set `SPRING_KAFKA_CONSUMER_BOOTSTRAP_SERVERS` and `SPRING_KAFKA_PRODUCER_BOOTSTRAP_SERVERS`).

user-service's `application-local.yml` is not usable as-is: its keys are nested under a second `spring:` level. See [Known issues](known-issues.md).

## 4. Run

Pick one:

- **Docker Compose:** see [docker.md](docker.md). Fastest for a full local stack.
- **Kubernetes (Docker Desktop / minikube):** see [kubernetes.md](kubernetes.md).
- **IDE:** start Kafka, Keycloak and Eureka with Compose, then run the services from the IDE with the env vars above.

Start order: Keycloak DB → Keycloak → Kafka → Eureka → gateway → user → account → customer.

## 5. First-time setup

1. Configure Keycloak (realm, client, secret): [security-keycloak.md → First-time setup](security-keycloak.md#first-time-keycloak-setup).
2. Create the application admin: `POST http://localhost:8081/api/v1/users/create-admin`.
3. Log in: `POST /api/v1/users/login` with `{"email":"admin@gmail.com","passwordHash":"<ADMIN_INITIAL_PASSWORD>"}`, then change the password.
4. Create the `Customer` role (`POST /api/v1/roles/add` with `{"name":"Customer"}`).
5. Create a customer (`POST /api/v1/customers/create-customer`). This exercises the whole saga: customer → user → account → email.

## Smoke test

```bash
GW=http://localhost:8081      # Kubernetes: http://localhost:30881
TOKEN=$(curl -s -X POST $GW/api/v1/users/login -H 'Content-Type: application/json' \
  -d "{\"email\":\"admin@gmail.com\",\"passwordHash\":\"$ADMIN_INITIAL_PASSWORD\"}" | jq -r .data.token.accessToken)

curl -s $GW/api/v1/dashboard/admin -H "Authorization: Bearer $TOKEN" | jq
curl -s "$GW/api/v1/customers/view-all-customers?size=5" -H "Authorization: Bearer $TOKEN" | jq
```
