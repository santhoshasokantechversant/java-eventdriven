#!/usr/bin/env bash
# Deploys the whole stack in dependency order.
# Run from the repository root:  ./k8s/deploy-all.sh
set -euo pipefail

if [ ! -f k8s/00-config/secrets.yaml ]; then
  echo "Missing k8s/00-config/secrets.yaml"
  echo "Copy k8s/00-config/secrets.example.yaml to secrets.yaml and fill in real values."
  exit 1
fi

echo "== 1. Config and secrets"
kubectl apply -f k8s/00-config/configmap.yaml
kubectl apply -f k8s/00-config/secrets.yaml

echo "== 2. Infrastructure (Keycloak DB, Keycloak, Kafka)"
kubectl apply -f k8s/01-infra/
kubectl rollout status deployment/keycloak-db --timeout=180s
kubectl rollout status deployment/kafka       --timeout=300s
kubectl rollout status deployment/keycloak    --timeout=300s

echo "== 3. Observability (ELK, Prometheus, Grafana)"
kubectl apply -f k8s/02-observability/

echo "== 4. Platform (Config Server, Eureka)"
kubectl apply -f k8s/03-platform/
kubectl rollout status deployment/eureka-server --timeout=300s

echo "== 5. Business services"
kubectl apply -f k8s/04-services/

echo "== 6. Ingress (optional; needs an nginx ingress controller)"
kubectl apply -f k8s/05-ingress/ || echo "Ingress skipped (no ingress controller?)"

echo
kubectl get pods
echo
echo "Gateway:  http://localhost:30881"
echo "Keycloak: http://localhost:30081   Eureka: http://localhost:30061"
echo "Kafka UI: http://localhost:30885   Kibana: http://localhost:30561"
echo "Grafana:  http://localhost:30300   Prometheus: http://localhost:30090"
