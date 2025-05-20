plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("io.ktor.plugin") version "2.3.12" // Reverted Ktor plugin version
    application
}

group = "com.example"
version = "1.0.0"

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // Added JitPack repository for Ktor-OpenAPI-Generator
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-server-core:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-server-openapi:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-server-swagger:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-server-status-pages:2.3.12") // Reverted Ktor version
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12") // Reverted Ktor version
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Explicitly define Logback version to ensure consistency
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("ch.qos.logback:logback-core:1.5.6")

    implementation("net.logstash.logback:logstash-logback-encoder:8.1") {
        exclude(group = "ch.qos.logback") // Exclude all transitive Logback dependencies
    }
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:2.16.0-alpha") // OTel Logback MDC appender

    testImplementation("io.ktor:ktor-server-tests:2.3.12") // Reverted Ktor version
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12") // Reverted Ktor version
    testImplementation("io.ktor:ktor-client-mock:2.3.12") // Reverted Ktor version
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}
