# Build from the repository root:  docker build -f user-service/Dockerfile .

# Stage 1: Build and install common-lib
FROM maven:3.9.6-eclipse-temurin-17 AS common-lib-build
WORKDIR /common-lib
COPY common-lib/pom.xml ./pom.xml
COPY common-lib/src ./src
RUN mvn -B -q clean install -DskipTests

# Stage 2: Build user-service using the installed common-lib
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY --from=common-lib-build /root/.m2 /root/.m2
COPY user-service/pom.xml ./pom.xml
COPY user-service/src ./src
RUN mvn -B -q clean package -DskipTests

# Stage 3: Runtime (JRE only, non-root)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system --gid 1001 app \
 && useradd --system --uid 1001 --gid app --home-dir /app app \
 && mkdir -p /app/logs \
 && chown -R app:app /app
COPY --from=build --chown=app:app /app/target/*.jar ./app.jar
USER 1001
# Heap follows the container memory limit; override with JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
