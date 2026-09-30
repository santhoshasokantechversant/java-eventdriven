# CI/CD (GitLab)

Pipeline file: [`.gitlab-ci.yml`](../.gitlab-ci.yml). All jobs run **only for the `production` branch** (commits to it, or merge requests targeting it).

## Stages and jobs

| Stage | Job | Image | What it does |
|---|---|---|---|
| build | `build-all` | `maven:3.9.6-eclipse-temurin-17` | Runs `build-all.sh`: installs common-lib, then packages every service with `-DskipTests`. Keeps `.m2/repository` and `*/target/` as artifacts for 2 hours. |
| build | `build-backend` | `docker:latest` + `docker:dind` | Logs in to Docker Hub, runs `docker-compose -f docker-compose-prod.yml build`, tags the six images as `$DOCKER_USERNAME/<service>:latest` and pushes them. |
| sast | GitLab SAST template | (GitLab) | Static analysis from `Security/SAST.gitlab-ci.yml` |
| sast | `spotbugs-sast` | maven | SpotBugs on **account-service only**, reusing the `build-all` artifacts |
| test | – | – | Defined but empty (a VPN test job is commented out) |
| deploy | – | – | Commented out: an SSH + VPN job that runs `docker-compose -f docker-compose-prod.yml pull && up -d` on the server |

## Required CI/CD variables

| Variable | Used by |
|---|---|
| `DOCKER_USERNAME`, `DOCKER_PASSWORD` | `build-backend` (mark as *masked*) |
| `VPN_CONFIG`, `VPN_USER`, `VPN_PASS`, `DEPLOY_SERVER_IP`, `DEPLOY_USER`, `DEPLOY_PASSWORD` | only if you re-enable the test/deploy jobs |

## Gaps and suggestions

| Gap | Suggestion |
|---|---|
| **Tests never run.** Every build uses `-DskipTests`, and the modules only contain the default context-load test. | Add unit tests, and a `test` job running `./mvnw verify` per module. Use Testcontainers (PostgreSQL, Kafka) for integration tests. |
| `docker-compose-prod.yml` has only `image:` entries and no `build:` sections, so `docker-compose build` builds nothing, and the following `docker tag techversant/...` steps fail unless those images already exist on the runner. | Build with the Dockerfiles directly (`docker build -f <svc>/Dockerfile .` for each service, as in `k8s/build-images.sh`). |
| Only `:latest` tags. You can't roll back or know what's deployed. | Also tag with `$CI_COMMIT_SHORT_SHA` and deploy that tag. |
| SpotBugs covers one service. | Loop over all modules. |
| Only runs on `production`. | Run build + test on every merge request; keep image push/deploy on `production`. |
| No Kubernetes deploy. | Add a job that runs `kubectl apply -f k8s/` with a kubeconfig stored as a CI variable, and sets the image tag. |
| Deploy uses password SSH. | Use an SSH key in a CI variable, or a GitLab agent for Kubernetes. |
