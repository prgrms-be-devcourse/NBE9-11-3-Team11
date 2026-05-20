plugins {
    java
    // 이 프로젝트 코틀린으로 컴파일할게"라고 선언
    kotlin("jvm") version "2.3.0"

    //Spring 어노테이션이 붙은 클래스를 open으로 변경해서 프록시로 감싸야해서 상속 가능하게 함
    kotlin("plugin.spring") version "2.3.0"

    //JPA 어노테이션이 붙은 부분에 기본 생성자 만들어줌
    //1. 기본 생성자로 빈 객체 먼저 생성
    //2. 리플렉션으로 각 필드에 값 주입 - 런타임에 클래스 정보를 동적으로 들여다보고 조작하는 기능
    kotlin("plugin.jpa") version "2.3.0"

    //Java에서는 annotationProcessor가 했는데 코틀린에서는 없음
    //Kotlin Annotation Processing Tool 을 추가해서 컴파일 하기전에 어노테이션을 스캔해서 코드 생성(QClass생성)
    kotlin("kapt") version "2.3.0"

    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.back.team11"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    // 코틀린 클래스 메타데이터 분석 (Spring Bean 생성, Jackson 역직렬화에 필요)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // 코틀린 data class JSON 직렬화/역직렬화 지원
    //Jackson 3.0부터 기존 com.fasterxml.jackson.* groupId가 tools.jackson.*으로 변경됨
    implementation("tools.jackson.module:jackson-module-kotlin")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // QueryDSL
    // com.querydsl:5.1.0은 오래된 버전이라 openfeign 사용
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")
    kapt("io.github.openfeign.querydsl:querydsl-apt:7.1:jpa")

    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // Spring Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // Spring
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")


    // 모니터링 - Spring Actuator (메트릭 엔드포인트 노출)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // 모니터링 - Prometheus 메트릭 수집 포맷 지원
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")

    // Lombok (Java 파일 변환 완료 후 제거)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    kapt("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Dev
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Runtime
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    // Redis
    implementation ("org.springframework.boot:spring-boot-starter-data-redis")

}

// kapt 상세 설정: 자바 어노테이션 프로세서와 충돌 방지
kapt {
    keepJavacAnnotationProcessors = true
}

kotlin {
    //코틀린 컴파일러 옵션 추가하는 블록
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// JPA 어노테이션 붙은 클래스를 자동으로 open 처리해주는 블록
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 일반 JAR 비활성화 - bootJar만 생성하여 Dockerfile에서 *.jar 와일드카드 사용 가능
// 버전 바뀌어도 Dockerfile 수정 불필요
tasks.named<Jar>("jar") {
    enabled = false
}
