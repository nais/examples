// Common
val kotlinVersion = "1.9.10"
val ktorVersion = "1.6.8"
val kotlinxVersion = "1.7.3"
val jacksonVersion = "2.15.3"
val konfigVersion = "1.6.10.0"
// Oauth2
val nimbusOIDC = "11.2"
val nimbusJoseVersion = "9.36"
val caffeineVersion = "3.1.8"
// Log
val apacheCommonsVersion = "3.13.0"
val logstashEncoderVersion = "7.4"
val logbackVersion = "1.4.11"
val ioPrometheusVersion = "0.16.0"
val kotlinloggingVersion = "3.0.5"
// Test
val spek = "2.0.13"
val kluentVersion = "1.73"
val wiremockVersion = "3.0.1"
val platformRunner = "1.10.0"
val mockOauth = "2.0.0"
val junitJupiterVersion = "5.10.0"

val mainClassName = "no.nav.dings.DebugKt"

plugins {
    kotlin("jvm") version "1.9.10"
    java
    id("org.jmailen.kotlinter") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.49.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = mainClassName
    }
    create("printVersion") {
        println(project.version)
    }
    withType<Test> {
        testLogging.events("passed", "skipped", "failed")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "14"
        }
    }
}

dependencies {
    implementation (kotlin("stdlib"))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation ("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation ("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation ("io.ktor:ktor-client-cio:$ktorVersion")
    implementation ("io.ktor:ktor-server-netty:$ktorVersion")
    implementation ("io.ktor:ktor-jackson:$ktorVersion")
    implementation ("io.ktor:ktor-client-core:$ktorVersion")
    implementation ("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-freemarker:$ktorVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
    implementation ("com.natpryce:konfig:$konfigVersion")
    implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation ("org.apache.commons:commons-lang3:$apacheCommonsVersion")
    implementation ("com.nimbusds:nimbus-jose-jwt:$nimbusJoseVersion")
    implementation("com.nimbusds:oauth2-oidc-sdk:${nimbusOIDC}")
    implementation ("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
    implementation ("ch.qos.logback:logback-classic:$logbackVersion")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    implementation ("io.prometheus:simpleclient_hotspot:$ioPrometheusVersion")
    implementation ("io.prometheus:simpleclient_common:$ioPrometheusVersion")
    implementation ("io.github.microutils:kotlin-logging:$kotlinloggingVersion")

    testImplementation ("no.nav.security:mock-oauth2-server:$mockOauth")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.platform:junit-platform-runner:$platformRunner")
    testImplementation ("com.github.tomakehurst:wiremock:$wiremockVersion")
}
