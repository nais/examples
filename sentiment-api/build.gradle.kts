import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("com.github.ben-manes.versions") version "0.52.0"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.spring") version "2.1.20"
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.google.cloud:google-cloud-language:2.62.0")
	implementation("org.jsoup:jsoup:1.20.1")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
