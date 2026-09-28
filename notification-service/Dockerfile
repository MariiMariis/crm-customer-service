FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -pl notification-service -am package -DskipTests
RUN cp notification-service/target/notification-service.jar app.jar && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
ARG OTEL_AGENT_VERSION=2.31.1
RUN addgroup -S crm && adduser -S crm -G crm
WORKDIR /app
ADD --chown=crm:crm https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build --chown=crm:crm /workspace/extracted/dependencies/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=crm:crm /workspace/extracted/application/ ./
USER crm
ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    OTEL_SERVICE_NAME=notification-service \
    OTEL_RESOURCE_ATTRIBUTES=service.namespace=nexo-crm \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318 \
    OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf \
    OTEL_METRIC_EXPORT_INTERVAL=15000 \
    OTEL_INSTRUMENTATION_SPRING_SCHEDULING_ENABLED=false
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 CMD wget -qO- http://localhost:8081/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
