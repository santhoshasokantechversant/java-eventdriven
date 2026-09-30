# Kubernetes

All manifests are in [`k8s/`](../k8s). They target a local single-node cluster (Docker Desktop Kubernetes or minikube) in the `default` namespace, using locally built images.

## Layout

```
k8s/
├── 00-config/
│   ├── configmap.yaml          shared non-secret settings (envFrom in every service)
│   └── secrets.example.yaml    TEMPLATE → copy to secrets.yaml (gitignored) and fill in
├── 01-infra/
│   ├── keycloak-db.yaml        Postgres for Keycloak (PVC 1Gi)
│   ├── keycloak.yaml           Keycloak 26 (NodePort 30081)
│   ├── kafka.yaml              Kafka 7.5 KRaft single node (PVC 2Gi)
│   └── kafka-ui.yaml           Kafka UI (NodePort 30885)
├── 02-observability/
│   ├── elasticsearch.yaml      (PVC 2Gi)
│   ├── logstash.yaml           TCP 5000 → Elasticsearch
│   ├── kibana.yaml             (NodePort 30561)
│   ├── prometheus.yaml         scrapes the 4 services (NodePort 30090)
│   └── grafana.yaml            Prometheus data source pre-provisioned (NodePort 30300)
├── 03-platform/
│   ├── configserver.yaml
│   └── eureka-server.yaml      (NodePort 30061)
├── 04-services/
│   ├── gateway-service.yaml    (NodePort 30881, the only public API port)
│   ├── user-service.yaml       (ClusterIP)
│   ├── account-service.yaml    (ClusterIP)
│   └── customer-service.yaml   (ClusterIP)
├── 05-ingress/ingress.yaml     optional: micro.local/api → gateway
├── build-images.sh / .bat      builds the 6 app images
└── deploy-all.sh / .bat        applies everything in order
```

The application database is **external**: set `SPRING_DATASOURCE_URL` in `secrets.yaml`. The Postgres in the cluster is only for Keycloak.

## Deploy

```bash
# 0. Use the local cluster
kubectl config use-context docker-desktop        # or: minikube start && eval $(minikube docker-env)

# 1. Build the images (from repo root)
./k8s/build-images.sh                            # Windows: k8s\build-images.bat

# 2. Create your secrets file
cp k8s/00-config/secrets.example.yaml k8s/00-config/secrets.yaml
#    edit secrets.yaml: DB URL/user/password, Keycloak client secret + admin, SMTP, AES key

# 3. Review k8s/00-config/configmap.yaml (frontend URLs etc.)

# 4. Deploy
./k8s/deploy-all.sh                              # Windows: k8s\deploy-all.bat

# 5. Watch it come up
kubectl get pods -w
```

Then do the [first-time Keycloak setup](security-keycloak.md#first-time-keycloak-setup) (realm, client, secret). If you change the client secret, update `secrets.yaml`, re-apply it, and restart the services:

```bash
kubectl apply -f k8s/00-config/secrets.yaml
kubectl rollout restart deployment gateway-service user-service account-service customer-service
```

## URLs

| URL | Component |
|---|---|
| `http://localhost:30881` | **API gateway**: all `/api/**` calls |
| `http://localhost:30081` | Keycloak admin console |
| `http://localhost:30061` | Eureka dashboard |
| `http://localhost:30885` | Kafka UI |
| `http://localhost:30561` | Kibana |
| `http://localhost:30090` | Prometheus |
| `http://localhost:30300` | Grafana |
| `http://micro.local/api/...` | Gateway via ingress (needs an nginx ingress controller and a hosts entry `127.0.0.1 micro.local`) |

user-, account- and customer-service are **ClusterIP only**. Their security trusts the `X-Gateway-Auth` header, so exposing them would let anyone skip the gateway's checks. For debugging, use a temporary port-forward:

```bash
kubectl port-forward svc/user-service 8082:8082
```

## Configuration model

Every business service gets its settings from two objects via `envFrom`:

| Object | Kind | Holds |
|---|---|---|
| `microservices-config` | ConfigMap | Eureka URL, Kafka bootstrap, Keycloak URL/realm/client id, JWT issuer, gateway route targets, service-to-service base URL, frontend links, JVM options |
| `microservices-secrets` | Secret | DB URL/user/password, Keycloak client secret + admin credentials, SMTP credentials, AES key |
| `keycloak-db-secret` | Secret | Keycloak Postgres db/user/password |

Spring Boot's relaxed binding maps env vars to properties. For example, `SERVICE_URL` → `service.url`, `USER_SERVICE_URL` → `user.service.url`, `SPRING_KAFKA_BOOTSTRAP_SERVERS` → `spring.kafka.bootstrap-servers`.

To change a value: edit the file → `kubectl apply -f …` → `kubectl rollout restart deployment/<name>` (pods don't reload env vars on their own).

## Health probes

| Component | Startup | Readiness | Liveness |
|---|---|---|---|
| gateway / user / account / customer / configserver / eureka-server | `GET /actuator/health/liveness`, up to 5 min | `GET /actuator/health/readiness` | `GET /actuator/health/liveness` |
| keycloak | `GET :9000/health/started` | `:9000/health/ready` | `:9000/health/live` |
| kafka | – | TCP 9092 | TCP 9092 |

The Spring services enable Boot's Kubernetes probe groups (`management.endpoint.health.probes.enabled: true`). Liveness and readiness track the application's own state and **don't include the database or Kafka**, so an outage elsewhere doesn't trigger restart loops. The full `/actuator/health` (with DB details) is still there for humans and dashboards.

## Graceful shutdown

When a pod stops, Kubernetes runs `preStop` (sleep 10 s, so the Service stops sending it traffic), then sends SIGTERM. Spring's graceful shutdown (`server.shutdown: graceful`, 20 s timeout) finishes in-flight HTTP requests and Kafka batches. `terminationGracePeriodSeconds: 45` covers both.

## Pod security

The images run as a non-root user (uid 1001) on a JRE base image. The manifests enforce `runAsNonRoot`, drop all Linux capabilities, and block privilege escalation.

## Resources

Business services: request 512Mi / 250m, limit 1Gi. The JVM heap is capped at 75% of the limit (`JAVA_TOOL_OPTIONS`). Elasticsearch: 1.5–2Gi. Plan for roughly **8 GB of RAM** for the whole stack. On Docker Desktop, raise the memory in *Settings → Resources*.

## What changed from the old `k8s/` and `k8s1/` folders

Both old folders were removed and replaced by this one. The old `k8s/` was an early draft: placeholder images, wrong service names (`eureka`, `gateway`), a deployment for common-lib (which is a library, not a service), and missing env vars. The new folder is based on `k8s1/`, with these fixes:

| Problem in `k8s1/` | Effect | Fix |
|---|---|---|
| Passwords, client secret, SMTP password, AES key written inline in every deployment; `secrets.yaml`/`configmap.yaml` present but unused | Credentials in git; duplicated in 4 files | ConfigMap + Secret via `envFrom`; real secrets file gitignored |
| Image names `javaPoc/...` | Invalid (uppercase) | `javapoc/...` |
| Service-to-service URLs never set | Services called the hardcoded `http://10.1.0.47:8081` from inside the cluster | `SERVICE_URL`, `MAIN_SERVICE_URL`, `USER_SERVICE_URL` → `http://gateway-service:8081` |
| Kafka: internal topics defaulted to replication factor 3 on a single broker; bitnami env vars on a Confluent image; `emptyDir` storage | Consumer groups can't form; data lost on restart | RF 1 for internal topics, unused env removed, PVC |
| eureka-server probed at `/actuator/health` | eureka had no actuator → endless restarts | Actuator added to eureka; probes use `/actuator/health/liveness` and `/readiness` |
| Keycloak had no probes; used deprecated `KEYCLOAK_ADMIN` | Services start before Keycloak is ready | `KC_HEALTH_ENABLED` + probes on :9000; `KC_BOOTSTRAP_ADMIN_*` |
| Logstash pipeline `beats 5044 → stdout` | Nothing reached Elasticsearch | Repo pipeline: TCP 5000 json_lines → Elasticsearch |
| Prometheus scraped eureka and configserver | Targets always down | The 4 services + eureka (now has actuator) |
| Ingress `rewrite-target: /` on every path | `/api/v1/users/login` was rewritten to `/` | `/api` only, no rewrite |
| user/account/customer exposed as NodePorts | Gateway permission checks could be bypassed with a forged header | ClusterIP |
| Only the gateway had resource limits; long fixed `initialDelaySeconds` | Unbounded memory; slow or flaky startup | Requests/limits everywhere; startup probes |
| Unused env (`KAFKA_CONSUMER_GROUP` copied as `user-service-group` into customer-service, SMTP on account/gateway, `SPRING_CLOUD_CONFIG_URI`) | Confusing | Removed |
| `deploy-all.bat` applied everything at once | Services started before Kafka/Keycloak were ready | Ordered deploy with `rollout status` waits |

## Troubleshooting

| Symptom | Check |
|---|---|
| Pod `ErrImageNeverPull` / `ImagePullBackOff` for `javapoc/*` | Images not built in the cluster's Docker. Run `build-images`; with minikube run `eval $(minikube docker-env)` first. |
| Service `CrashLoopBackOff`, log says `Could not resolve placeholder` | A required env var is missing: compare `secrets.yaml` with `secrets.example.yaml`. |
| Service not ready; log shows DB connection refused | `SPRING_DATASOURCE_URL` wrong, or the cluster can't reach the external DB. |
| Login returns "Something is wrong with key-clock" | Realm/client not set up, or wrong `KEYCLOAK_CLIENT_SECRET`. |
| Every API call returns 401 | Token issuer mismatch: tokens must come from `http://keycloak:8080/realms/bank_system` (log in through the gateway, not directly against `localhost:30081`). |
| Create customer times out (30 s) | `kubectl logs deploy/user-service` and `deploy/account-service`; check the `Customer` role exists; check Kafka consumer groups in Kafka UI. |
| Kafka pod restarting | `kubectl logs deploy/kafka`; delete the PVC to reformat storage after a config change: `kubectl delete pvc kafka-data-pvc`. |

Useful commands:
```bash
kubectl get pods,svc
kubectl logs -f deploy/customer-service
kubectl describe pod <pod>
kubectl exec -it deploy/kafka -- kafka-consumer-groups --bootstrap-server localhost:9092 --list
kubectl delete -f k8s/04-services/ && kubectl apply -f k8s/04-services/
```

## Tear down

```bash
kubectl delete -f k8s/05-ingress/ -f k8s/04-services/ -f k8s/03-platform/ -f k8s/02-observability/ -f k8s/01-infra/ -f k8s/00-config/configmap.yaml -f k8s/00-config/secrets.yaml
# PVCs (Keycloak DB, Kafka, Elasticsearch data) are deleted with their files above.
```
