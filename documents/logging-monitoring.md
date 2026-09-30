# Logging & monitoring

## Application logs

Each Spring Boot service configures Logback in `src/main/resources/logback-spring.xml`:

| Appender | Destination | Format | Content |
|---|---|---|---|
| `STDOUT` | console | text | everything at INFO+ (plus DEBUG for `com.techversant` and Kafka, from `application.yml`) |
| `FILE` | `/app/logs/<service>/app.log` (daily, 7 days kept) | text | same as console |
| `AUDIT_FILE` | `/app/logs/<service>/audit.log` (daily, 30 days kept) | JSON | only the audit logger `com.techversant.security` |

In Kubernetes, read logs with `kubectl logs deploy/<service>`. The files are inside the container and disappear with it.

### Audit log

Written via `AuditLogger.log(user, ip, entity, action, status)` from common-lib. Example line:

```json
{"timestamp":"2026-09-28T10:15:30.123+0530","user":"admina1234","ip":"10.1.0.12",
 "entity":"Security","action":"CREATE_CUSTOMER: 3f2c…","status":"SUCCESS","service":"customer-service"}
```

Logged events include create/update/delete in the controllers, login and password-reset outcomes in user-service, and 401/403 at the gateway.

> The JSON pattern hardcodes `"entity": "Security"` instead of `%X{entity}`, so the entity passed to `AuditLogger` is lost. Change it to `"entity": "%X{entity:-unknown}"` in each `logback-spring.xml`.

## ELK (Elasticsearch, Logstash, Kibana)

| Component | Docker | Kubernetes | Notes |
|---|---|---|---|
| Elasticsearch 8.15 | `:9200` | ClusterIP `elasticsearch:9200` | single node, security off |
| Logstash 8.15 | `:5000` (TCP), `:9600` | ClusterIP `logstash:5000` | pipeline `logging/logstash/logstash.conf` |
| Kibana 8.15 | `:8889` | `http://localhost:30561` | |

Pipeline: TCP 5000, `json_lines` → Elasticsearch index `microservices-logs-YYYY.MM.dd`.

### ⚠️ Logs are not shipped yet

No service has an appender that sends to Logstash; they only write to console and files. To ship logs, add this to each `logback-spring.xml` (the `logstash-logback-encoder` dependency is already present):

```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>${LOGSTASH_HOST:-logstash:5000}</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service":"${applicationName}"}</customFields>
    </encoder>
</appender>

<root level="INFO">
    <appender-ref ref="STDOUT"/>
    <appender-ref ref="FILE"/>
    <appender-ref ref="LOGSTASH"/>
</root>
```

Then in Kibana: *Stack Management → Data Views → Create*, pattern `microservices-logs-*`, time field `@timestamp`.

## Metrics (Prometheus + Grafana)

gateway, user, account and customer services expose Actuator endpoints `health`, `info`, `metrics`, `prometheus` on their main port:

```
GET /actuator/health        # health incl. DB, disk (details shown)
GET /actuator/prometheus    # Micrometer metrics for Prometheus
```

| Tool | Kubernetes URL | Config |
|---|---|---|
| Prometheus | `http://localhost:30090` | `k8s/02-observability/prometheus.yaml`: scrapes the 4 services and eureka-server every 15 s |
| Grafana | `http://localhost:30300` | Prometheus pre-configured as the default data source; login = Keycloak admin credentials from the secret |

configserver doesn't expose `prometheus`, so it isn't scraped.

**Useful dashboards:** import Grafana dashboard **4701** (JVM Micrometer) or **11378** (Spring Boot statistics) and select the Prometheus data source.

**Useful queries:**
```promql
rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m])                  # request rate
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket[5m])))  # p95 latency
sum by (application) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))  # 5xx rate
jvm_memory_used_bytes{area="heap"}
resilience4j_circuitbreaker_state
```

## Not yet in place

- **Distributed tracing.** There is no Micrometer Tracing / Zipkin / OpenTelemetry. To follow one request across gateway → customer → user → account, add `micrometer-tracing-bridge-otel` + an exporter, and put `traceId` in the log pattern.
- **Alerting.** No Prometheus alert rules or Alertmanager.
