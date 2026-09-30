#!/bin/bash
echo "=== Running build-all.sh ==="

# Ensure Maven wrapper is executable
chmod +x ./mvnw || true

# Build common-lib
echo "=== Building common-lib ==="
cd common-lib
./mvnw clean install -DskipTests || mvn clean install -DskipTests
cd ..

# Build account-service
echo "=== Building account-service ==="
cd account-service
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

# Build user-service
echo "=== Building user-service ==="
cd user-service
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

# Build customer-service
echo "=== Building customer-service ==="
cd customer-service
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

# Build gateway-service
echo "=== Building gateway-service ==="
cd gateway-service
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

# Build configserver
echo "=== Building configserver ==="
cd configserver
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

# Build eureka-server
echo "=== Building eureka-server ==="
cd eureka-server
./mvnw clean package -DskipTests || mvn clean package -DskipTests
cd ..

echo "=== Build-all.sh completed ==="
