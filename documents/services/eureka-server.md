# eureka-server

| | |
|---|---|
| **Port** | 8761 |
| **Source** | [`eureka-server/`](../../eureka-server) |
| **Dashboard** | `http://localhost:8761` (Docker) · `http://localhost:30061` (Kubernetes) |

## What it does

A Netflix Eureka service registry. gateway-service, user-service, account-service and customer-service register at startup (`eureka.client.service-url.defaultZone = http://eureka-server:8761/eureka`) with `prefer-ip-address: true`. The dashboard shows which instances are up.

The gateway does **not** use Eureka for routing today (it uses fixed URLs), and the Feign clients use explicit `url`s. Eureka currently gives you visibility, not load balancing.

## Configuration

`application.yml`:
```yaml
server.port: 8761
eureka.client.register-with-eureka: false
eureka.client.fetch-registry: false
```
No environment variables are required.

## Health checks and metrics

Spring Boot Actuator is included:

| Endpoint | Use |
|---|---|
| `GET /actuator/health/liveness` | Kubernetes liveness + startup probe |
| `GET /actuator/health/readiness` | Kubernetes readiness probe |
| `GET /actuator/health` | overall health (also used by the docker-compose healthcheck) |
| `GET /actuator/prometheus` | metrics (scraped by Prometheus) |

## Build

```bash
docker build -t javapoc/eureka-server:local -f eureka-server/Dockerfile .   # run from repo root
```
