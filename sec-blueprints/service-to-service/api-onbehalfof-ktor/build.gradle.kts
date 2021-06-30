import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by rootProject.extra
val logbackVersion: String by rootProject.extra
val konfigVersion: String by rootProject.extra
val kotlinResultVersion: String by rootProject.extra
val mockWebServerVersion: String by rootProject.extra
val junitJupiterVersion: String by rootProject.extra
val mockkVersion: String by rootProject.extra
val kotlinVersion: String by rootProject.extra
val mockOAuth2ServerVersion: String by rootProject.extra

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.plugin.serialization")
}

repositories {
    maven(url="https://dl.bintray.com/michaelbull/maven")
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
    implementation("com.michael-bull.kotlin-result:kotlin-result:$kotlinResultVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

application {
    mainClassName = "no.nav.security.examples.ProtectedOnBehalfOfAppKt"
}

tasks {
    withType<ShadowJar> {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "no.nav.security.examples.ProtectedOnBehalfOfAppKt"
                )
            )
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    "build" {
        dependsOn("shadowJar")
    }
}
