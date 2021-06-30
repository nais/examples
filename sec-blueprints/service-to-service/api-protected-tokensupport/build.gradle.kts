val wiremockVersion: String by rootProject.extra
val nimbusSdkVersion: String by rootProject.extra
val tokenSupportVersion: String by rootProject.extra

plugins {
    java
    id("org.springframework.boot")
}

apply(plugin = "io.spring.dependency-management")

repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    mavenCentral()
    jcenter()

}

dependencies {
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("com.github.tomakehurst:wiremock:$wiremockVersion")
    testImplementation("junit:junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
