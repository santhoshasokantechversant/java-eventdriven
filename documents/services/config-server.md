# configserver (Spring Cloud Config Server)

| | |
|---|---|
| **Port** | 8888 |
| **Source** | [`configserver/`](../../configserver) |
| **Profile used in Docker/K8s** | `native` |

## What it does

Serves configuration over HTTP (`GET /{application}/{profile}`). It supports two backends:

- **`native`** (what Docker Compose and Kubernetes run): serves files from the classpath (`classpath:/`, `classpath:/config`). None are bundled, so it serves nothing useful yet.
- **git** (the default when no profile is set): reads from the GitLab repo in `application.yml`. Credentials come from `CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD`.

## Current status: not consumed

No service uses the config server yet. None of them has the `spring-cloud-starter-config` dependency or a `spring.config.import=configserver:` setting, so the `SPRING_CLOUD_CONFIG_URI` variables set in docker-compose have no effect. Each service reads its own `application.yml` plus environment variables.

To adopt it:
1. Add `spring-cloud-starter-config` to each service's `pom.xml`.
2. Add `spring.config.import: optional:configserver:http://configserver:8888` to each service's `application.yml`.
3. Put shared config (Kafka, Eureka, Resilience4j) into the config repo as `application.yml`, and per-service overrides as `<service-name>.yml`.
4. Keep secrets out of the config repo, in environment variables / Kubernetes Secrets.

## Configuration

| Env var | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `native` (Docker/K8s) or unset for git |
| `CONFIG_GIT_USERNAME`, `CONFIG_GIT_PASSWORD` | Git credentials (git backend only) |

> A GitLab personal access token used to be hardcoded in `application.yml`. It has been replaced with these variables. **Revoke that token in GitLab.** It's still in git history.

## Health

Actuator is included: `GET /actuator/health`.
