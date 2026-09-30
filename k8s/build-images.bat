@echo off
REM Builds the six Spring Boot images with the tags the k8s manifests expect.
REM Run from the repository root:  k8s\build-images.bat
REM For minikube, first run:  minikube docker-env | Invoke-Expression   (PowerShell)

docker build -t javapoc/configserver:local     .\configserver || exit /b 1
docker build -t javapoc/eureka-server:local    -f eureka-server\Dockerfile    . || exit /b 1
docker build -t javapoc/gateway-service:local  -f gateway-service\Dockerfile  . || exit /b 1
docker build -t javapoc/user-service:local     -f user-service\Dockerfile     . || exit /b 1
docker build -t javapoc/account-service:local  -f account-service\Dockerfile  . || exit /b 1
docker build -t javapoc/customer-service:local -f customer-service\Dockerfile . || exit /b 1

echo All images built.
