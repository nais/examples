FROM gradle:jdk17 as build
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
FROM scratch as javaagent
ARG JAVA_OTEL_VERSION=v1.31.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$JAVA_OTEL_VERSION/opentelemetry-javaagent.jar /instrumentations/java/javaagent.jar

# Final image
FROM gcr.io/distroless/java17:nonroot
COPY --from=javaagent --chown=nonroot:nonroot /instrumentations/java/javaagent.jar /app/javaagent.jar
COPY --from=build --chown=nonroot:nonroot /home/gradle/src/build/libs/sentiment-api-*.jar /app/shop-backend.jar
WORKDIR /app
# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-javaagent:/app/javaagent.jar", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "/app/sentiment-api.jar" ]