FROM gradle:jdk17 as build
WORKDIR /home/gradle/src

# Download dependencies to cache them
COPY --chown=gradle:gradle gradlew *.gradle *.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle/
RUN ./gradlew --no-daemon assemble || true

# Build the application
COPY --chown=gradle:gradle src ./src/
RUN ./gradlew --no-daemon assemble

FROM gcr.io/distroless/java17:nonroot
COPY --from=build --chown=nonroot:nonroot /home/gradle/src/build/libs/db-dings-*.jar /app/db-dings.jar
WORKDIR /app
# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "/app/db-dings.jar"]