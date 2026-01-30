plugins {
    id("java")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "io.news"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // Thymeleaf Layout Dialect (레이아웃 기능)
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0")

    // RSS 파싱 (Rome)
    implementation("com.rometools:rome:2.1.0")
    implementation("com.rometools:rome-modules:2.1.0")

    // HTML 파싱 (Jsoup)
    implementation("org.jsoup:jsoup:1.17.2")

    // Elasticsearch
    implementation("co.elastic.clients:elasticsearch-java:8.12.0")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // DevTools (개발 시 자동 리로드)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
