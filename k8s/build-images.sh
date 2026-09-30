#!/usr/bin/env bash
# Builds the six Spring Boot images with the tags the k8s manifests expect.
# Run from the repository root:  ./k8s/build-images.sh
# For minikube, first point Docker at minikube:  eval $(minikube docker-env)
set -euo pipefail

docker build -t javapoc/configserver:local     ./configserver
docker build -t javapoc/eureka-server:local    -f eureka-server/Dockerfile    .
docker build -t javapoc/gateway-service:local  -f gateway-service/Dockerfile  .
docker build -t javapoc/user-service:local     -f user-service/Dockerfile     .
docker build -t javapoc/account-service:local  -f account-service/Dockerfile  .
docker build -t javapoc/customer-service:local -f customer-service/Dockerfile .

echo "All images built."
