group = "no.nav"
version = "0.0.1"

object Versions {
    const val tokenSupport = "1.3.0"
    const val ktorVersion = "1.3.2"
    const val kluentVersion = "1.61"
    const val platformRunner = "1.5.1"
    const val wiremockVersion = "2.26.3"
    const val juniper = "5.6.2"
    const val juniperPlatform = "1.6.2"
    const val logstashEncoder = "6.4"
    const val kotlinloggingVersion = "1.8.3"
}

val mainClassName = "no.nav.dings.ApiDingsKt"

plugins {
    kotlin("jvm") version "1.3.72"
    java
    id("org.jmailen.kotlinter") version "2.3.2"
    id("com.github.ben-manes.versions") version "0.45.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "http://packages.confluent.io/maven/")
}

tasks {
    withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
        dependsOn("formatKotlin")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "13"
        }
    }
    withType<Jar> {
        manifest.attributes["Main-Class"] = mainClassName
    }
    create("printVersion") {
        println(project.version)
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging.events("passed", "skipped", "failed")
    }
}

dependencies {
    implementation (kotlin("stdlib"))
    implementation ("io.ktor:ktor-auth:${Versions.ktorVersion}")
    implementation ("io.ktor:ktor-auth-jwt:${Versions.ktorVersion}")
    implementation ("io.ktor:ktor-server-netty:${Versions.ktorVersion}")
    implementation ("io.ktor:ktor-jackson:${Versions.ktorVersion}")
    implementation ("io.ktor:ktor-client-core:${Versions.ktorVersion}")
    implementation ("io.ktor:ktor-client-jackson:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${Versions.ktorVersion}")
    implementation ("no.nav.security:token-validation-ktor:${Versions.tokenSupport}")
    implementation("no.nav.security:token-validation-test-support:${Versions.tokenSupport}")
    implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logstashEncoder}")
    implementation ("io.github.microutils:kotlin-logging:${Versions.kotlinloggingVersion}")

    testImplementation("io.ktor:ktor-server-test-host:${Versions.ktorVersion}") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("org.amshove.kluent:kluent:${Versions.kluentVersion}")
    testImplementation("org.junit.platform:junit-platform-runner:${Versions.platformRunner}")
    testImplementation ("com.github.tomakehurst:wiremock:${Versions.wiremockVersion}")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:${Versions.juniper}")
    testImplementation ("org.junit.jupiter:junit-jupiter-api:${Versions.juniper}")
    testImplementation ("org.junit.platform:junit-platform-launcher:${Versions.juniperPlatform}")

}
