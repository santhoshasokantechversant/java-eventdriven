# Docker

## Service images

Every Spring Boot service has a multi-stage Dockerfile:

| Stage | Base image | Does |
|---|---|---|
| 1. `common-lib-build` | `maven:3.9.6-eclipse-temurin-17` | builds and installs `common-lib` into `/root/.m2` |
| 2. `build` | `maven:3.9.6-eclipse-temurin-17` | copies stage 1's `.m2`, runs `mvn clean package -DskipTests` |
| 3. runtime | `eclipse-temurin:17-jre-jammy` | copies the jar to `/app/app.jar`, runs `java -jar app.jar` as non-root user `app` (uid 1001), with `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`; `/app/logs` is writable for the file appenders |

(configserver and eureka-server don't depend on common-lib, so they have only the build and runtime stages.)

**Build context:** the four common-lib services and eureka-server must be built **from the repository root**, because their Dockerfiles copy `common-lib/` and `<service>/` from there:

```bash
docker build -t javapoc/configserver:local     ./configserver
docker build -t javapoc/eureka-server:local    -f eureka-server/Dockerfile    .
docker build -t javapoc/gateway-service:local  -f gateway-service/Dockerfile  .
docker build -t javapoc/user-service:local     -f user-service/Dockerfile     .
docker build -t javapoc/account-service:local  -f account-service/Dockerfile  .
docker build -t javapoc/customer-service:local -f customer-service/Dockerfile .
```
(`k8s/build-images.sh` / `.bat` run exactly these.)

Image names must be **lowercase** (`javapoc/...`).

| Service | Exposed port |
|---|---|
| gateway-service | 8081 |
| user-service | 8082 |
| account-service | 8083 |
| customer-service | 8084 |
| eureka-server | 8761 |
| configserver | 8888 |

### Possible next improvement
- Cache dependencies: `COPY pom.xml` → `RUN mvn dependency:go-offline` before `COPY src`, so code changes don't re-download everything.

## Infrastructure images (`infra/`)

Each `infra/<name>/Dockerfile` is a single `FROM` line that pins a version: postgres:15, keycloak 26.0.0, cp-kafka 7.5.0, kafka-ui latest, elasticsearch/kibana/logstash 8.15.0. `docker-compose-build.yml` uses them. The Kubernetes manifests use the upstream images directly.

## Compose files

| File | Purpose | Builds from source? | Notes |
|---|---|---|---|
| `docker-compose.yml` | Full local stack for development | yes | Kafka = bitnami 3.7; ELK built from `logging/`; app DB is **external** (`10.1.0.47:5432`) |
| `docker-compose-build.yml` | Builds and tags every image as `javapoc/*:local` (for Kubernetes) | yes | includes its own Postgres on `5433` |
| `docker-compose-prod.yml` | Runs pre-built `techversant/*:latest` images (used by CI) | no | Elasticsearch with a password |
| `kafka/docker-compose-kafka.yml` | Standalone Kafka + UI for experiments | – | separate network |
| `keyclock/docker-compose-keycloak.yml` | Standalone Keycloak + its DB | – | separate network |

### Run the full stack

```bash
./build-all.sh                         # optional; the images build from source anyway
docker compose up -d --build
docker compose ps
docker compose logs -f customer-service
```

### Ports (docker-compose.yml)

| URL | Component |
|---|---|
| `http://localhost:8081` | **Gateway (API entry point)** |
| `http://localhost:8082` / `8083` / `8084` | user / account / customer services (direct; bypasses gateway checks) |
| `http://localhost:8761` | Eureka dashboard |
| `http://localhost:8888` | Config server |
| `http://localhost:8182` | Keycloak admin console |
| `localhost:9092` | Kafka (advertised as `kafka:9092`, so host clients need a hosts entry) |
| `http://localhost:8585` | Kafka UI |
| `http://localhost:9200` | Elasticsearch |
| `http://localhost:8889` | Kibana |
| `localhost:5000` | Logstash TCP input |

### Problems in the current compose files

| File | Problem | Fix |
|---|---|---|
| all three | Secrets written inline (DB password, Keycloak client secret, SMTP password, AES key) | Use `${VAR}` references; Compose reads them from an untracked `.env` automatically. The committed `.env` already has the right variable names; untrack it (`git rm --cached .env`) and **rotate the values**. |
| `docker-compose.yml` | ✅ fixed: `SERVER_PORT=8082` on account- and customer-service moved the whole server to 8082 (Spring maps `SERVER_PORT` to `server.port`) | Now 8083 / 8084 |
| `docker-compose.yml`, `-prod` | ✅ fixed: eureka-server healthcheck called `/actuator/health`, which didn't exist | Actuator added to eureka-server |
| `docker-compose.yml`, `-prod` | Keycloak healthcheck `http://localhost:8080/health`: Keycloak 26 serves health on port 9000 and only with `KC_HEALTH_ENABLED=true` | Enable health and use `:9000/health/ready` |
| `docker-compose.yml` | ✅ fixed: service-to-service URLs were hardcoded to `http://10.1.0.47:8081` | They now default to `http://gateway-service:8081` (override with `SERVICE_URL`, `MAIN_SERVICE_URL`, `USER_SERVICE_URL`) |
| `docker-compose.yml` | Uses `KEYCLOAK_ADMIN` (deprecated in Keycloak 26) | `KC_BOOTSTRAP_ADMIN_USERNAME` / `KC_BOOTSTRAP_ADMIN_PASSWORD` |
| `docker-compose-build.yml` | ✅ fixed: image names `javaPoc/*:local` contained uppercase letters | Renamed to `javapoc/*:local` (matches the k8s manifests) |
| `SPRING_CLOUD_CONFIG_URI`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_CONSUMER_GROUP` | Not read by any service (no config client; Kafka settings are in `application.yml`) | Remove, or use `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
