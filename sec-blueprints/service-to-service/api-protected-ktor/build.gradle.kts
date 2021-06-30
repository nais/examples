import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktorVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra
val konfigVersion: String by rootProject.extra

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("com.natpryce:konfig:$konfigVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClassName = "no.nav.security.examples.ProtectedAppKt"
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "no.nav.security.examples.ProtectedAppKt"
                )
            )
        }
    }

    "build" {
        dependsOn("shadowJar")
    }
}
