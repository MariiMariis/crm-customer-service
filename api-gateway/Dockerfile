FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl api-gateway -am package -DskipTests
RUN cp api-gateway/target/api-gateway.jar app.jar && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S crm && adduser -S crm -G crm
WORKDIR /app
COPY --from=build --chown=crm:crm /workspace/extracted/dependencies/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/application/ ./
USER crm
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
