plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("io.ktor.plugin") version "2.3.4"
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
    implementation("io.ktor:ktor-server-netty:2.3.4") // Netty engine for Ktor
    implementation("io.ktor:ktor-server-core:2.3.4")
    implementation("io.ktor:ktor-server-openapi:2.3.4") // OpenAPI support
    implementation("io.ktor:ktor-server-swagger:2.3.4") // Swagger UI support
    implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-server-status-pages:2.3.4") // Added StatusPages dependency
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Explicitly define Logback version to ensure consistency
    implementation("ch.qos.logback:logback-classic:1.3.14")
    implementation("ch.qos.logback:logback-core:1.3.14")

    implementation("net.logstash.logback:logstash-logback-encoder:7.4") {
        exclude(group = "ch.qos.logback") // Exclude all transitive Logback dependencies
    }

    testImplementation("io.ktor:ktor-server-tests:2.3.4")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.4") // Added for content negotiation in tests
    testImplementation("io.ktor:ktor-client-mock:2.3.4") // Added for mocking client requests
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
    }
}
