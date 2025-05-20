plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("io.ktor.plugin") version "3.1.3"
    application
}

group = "io.nais.quotesbackend"
version = "1.0.0"

application {
    mainClass.set("io.nais.quotesbackend.ApplicationKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // For Ktor-OpenAPI-Generator
}

val ktorVersion = "2.3.13"
val kotlinxSerializationVersion = "1.8.1"
val logbackVersion = "1.5.18"
val logstashLogbackEncoderVersion = "8.1"
val opentelemetryVersion = "2.16.0-alpha"
val kotlinTestVersion = "2.1.21"

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
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
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion") {
        exclude(group = "ch.qos.logback") // Exclude transitive Logback to use our defined version
    }
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$opentelemetryVersion")

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
