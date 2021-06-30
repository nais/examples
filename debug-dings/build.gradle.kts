// Common
val kotlinVersion = "1.4.10"
val ktorVersion = "1.4.1"
val kotlinxVersion = "1.4.1"
val jacksonVersion = "2.11.2"
val konfigVersion = "1.6.10.0"
// Oauth2
val nimbusOIDC = "8.19"
val nimbusJoseVersion = "8.20"
val caffeineVersion = "2.8.6"
// Log
val apacheCommonsVersion = "3.11"
val logstashEncoderVersion = "6.4"
val logbackVersion = "1.2.3"
val ioPrometheusVersion = "0.9.0"
val kotlinloggingVersion = "2.0.3"
// Test
val spek = "2.0.13"
val kluentVersion = "1.61"
val wiremockVersion = "2.27.2"
val platformRunner = "1.7.0"
val mockOauth = "0.2.1"
val junitJupiterVersion = "5.7.0"

val mainClassName = "no.nav.dings.DebugKt"

plugins {
    kotlin("jvm") version "1.4.10"
    java
    id("org.jmailen.kotlinter") version "3.2.0"
    id("com.github.ben-manes.versions") version "0.36.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
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
