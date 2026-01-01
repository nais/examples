FROM gradle:9-jdk21-jammy AS build
WORKDIR /home/gradle/src

# Download dependencies to cache them
COPY --chown=gradle:gradle gradlew *.gradle *.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle/
RUN ./gradlew --no-daemon assemble || true

# Build the application
COPY --chown=gradle:gradle src ./src/
RUN ./gradlew --no-daemon assemble

# Download opentelemetry-javaagent
# https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/
FROM scratch AS javaagent
ARG JAVA_OTEL_VERSION=v2.0.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$JAVA_OTEL_VERSION/opentelemetry-javaagent.jar /instrumentations/java/javaagent.jar

# Final image
FROM gcr.io/distroless/java21-debian12:nonroot
# COPY --from=javaagent --chown=nonroot:nonroot /instrumentations/java/javaagent.jar /app/javaagent.jar
COPY --from=build --chown=nonroot:nonroot /home/gradle/src/build/libs/quotes-backend-*.jar /app/quotes-backend.jar
WORKDIR /app
# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1.2", "-Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener", "-jar", "/app/quotes-backend.jar" ]
