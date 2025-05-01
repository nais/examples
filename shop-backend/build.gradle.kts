import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("com.github.ben-manes.versions") version "0.52.0"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.spring") version "2.1.20"
	kotlin("plugin.jpa") version "2.1.20"
}

group = "no.nav"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencyLocking {
	lockAllConfigurations()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.0")

	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")

	//implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
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
