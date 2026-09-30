@echo off
REM Deploys the whole stack in dependency order.
REM Run from the repository root:  k8s\deploy-all.bat

if not exist k8s\00-config\secrets.yaml (
  echo Missing k8s\00-config\secrets.yaml
  echo Copy k8s\00-config\secrets.example.yaml to secrets.yaml and fill in real values.
  exit /b 1
)

echo == 1. Config and secrets
kubectl apply -f k8s\00-config\configmap.yaml || exit /b 1
kubectl apply -f k8s\00-config\secrets.yaml || exit /b 1

echo == 2. Infrastructure (Keycloak DB, Keycloak, Kafka)
kubectl apply -f k8s\01-infra\ || exit /b 1
kubectl rollout status deployment/keycloak-db --timeout=180s
kubectl rollout status deployment/kafka --timeout=300s
kubectl rollout status deployment/keycloak --timeout=300s

echo == 3. Observability (ELK, Prometheus, Grafana)
kubectl apply -f k8s\02-observability\ || exit /b 1

echo == 4. Platform (Config Server, Eureka)
kubectl apply -f k8s\03-platform\ || exit /b 1
kubectl rollout status deployment/eureka-server --timeout=300s

echo == 5. Business services
kubectl apply -f k8s\04-services\ || exit /b 1

echo == 6. Ingress (optional; needs an nginx ingress controller)
kubectl apply -f k8s\05-ingress\

echo.
kubectl get pods
echo.
echo Gateway:  http://localhost:30881
echo Keycloak: http://localhost:30081   Eureka: http://localhost:30061
echo Kafka UI: http://localhost:30885   Kibana: http://localhost:30561
echo Grafana:  http://localhost:30300   Prometheus: http://localhost:30090
