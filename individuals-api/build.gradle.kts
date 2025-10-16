plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.15.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "individuals-api"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
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

val versions = mapOf(
    "jakartaValidationApi" to "3.1.1",
    "micrometerRegistryPrometheus" to "1.15.4",
    "lokiLogbackAppender" to "2.0.0",
    "javaJwt" to "4.5.0",
    "jwksRsa" to "0.22.2",
    "logstashLogbackEncoder" to "8.1",
    "junitJupiter" to "1.21.3",
    "logbackClassicVersion" to "1.5.18",
    "testcontainers" to "1.21.3",
    "testcontainersKeycloak" to "3.8.0"
)

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    //openapi codegen libs
    implementation("jakarta.validation:jakarta.validation-api:${versions["jakartaValidationApi"]}")

    implementation("io.micrometer:micrometer-registry-prometheus:${versions["micrometerRegistryPrometheus"]}")
    implementation("com.github.loki4j:loki-logback-appender:${versions["lokiLogbackAppender"]}")
    implementation("com.auth0:java-jwt:${versions["javaJwt"]}")
    implementation("com.auth0:jwks-rsa:${versions["jwksRsa"]}")
    implementation("net.logstash.logback:logstash-logback-encoder:${versions["logstashLogbackEncoder"]}")
    implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:${versions["junitJupiter"]}")
    testImplementation("org.testcontainers:testcontainers:${versions["testcontainers"]}")
    testImplementation("com.github.dasniko:testcontainers-keycloak:${versions["testcontainersKeycloak"]}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set("${projectDir}/openapi/individuals-api.yml")
    outputDir.set(layout.buildDirectory.dir("generated-sources/openapi").get().toString())
    modelPackage.set("com.example.dto")
    globalProperties.set(
            mapOf(
                    "models" to "",
                    "apis" to "false",
                    "supportingFiles" to "false",
                    "modelDocs" to "false"
            )
    )
    configOptions.set(
            mapOf(
                    "serializationLibrary" to "jackson",
                    "dateLibrary" to "java8",
                    "useBeanValidation" to "true",
                    "useJakartaEe" to "true",
                    "serializableModel" to "true",
                    "library" to "webclient",
                    "openApiNullable" to "false"
            )
    )
    additionalProperties.set(
            mapOf(
                    "jackson" to "true",
                    "useJacksonAnnotations" to "true",
                    "gson" to "false"
            )
    )
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-sources/openapi/src/main/java"))
        }
    }
}

tasks {
    compileJava {
        dependsOn(openApiGenerate)
    }

    clean {
        delete(layout.buildDirectory.dir("generated-sources"))
    }

    withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
        mainClass.set("com.example.individualsapi.IndividualsApiApplication")
    }
}