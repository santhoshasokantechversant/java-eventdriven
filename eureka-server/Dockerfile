# Build from the repository root:  docker build -f eureka-server/Dockerfile .

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY eureka-server/pom.xml ./pom.xml
COPY eureka-server/src ./src
RUN mvn -B -q clean package -DskipTests

# Stage 2: Runtime (JRE only, non-root)
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
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
