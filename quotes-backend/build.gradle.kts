plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("io.ktor.plugin") version "2.3.12"
    application
    id("com.github.ben-manes.versions") version "0.46.0"
}

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

group = "io.nais.quotesbackend"
version = "1.0.0"

application {
    mainClass.set("io.nais.quotesbackend.ApplicationKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // For Ktor-OpenAPI-Generator
}

val ktorVersion = "2.3.12"
val kotlinxSerializationVersion = "1.6.3"
val logbackVersion = "1.5.6"
val logstashLogbackEncoderVersion = "8.1"
val opentelemetryVersion = "2.16.0-alpha"
val kotlinTestVersion = "1.9.20"

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion") {
        exclude(group = "ch.qos.logback") // Use defined Logback version
    }
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$opentelemetryVersion")
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:$opentelemetryVersion")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinTestVersion")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}
