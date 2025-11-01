plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.15.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
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
        "testcontainersKeycloak" to "3.8.0",
        "personApiVersion" to "1.0.0-SNAPSHOT",
        "feignMicrometerVersion" to "13.6",
        "springCloudStarterOpenfeign" to "4.1.1",
        "mapstructVersion" to "1.5.5.Final"
)

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
    }
}

dependencies {
    //starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:${versions["springCloudStarterOpenfeign"]}")

    //monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.micrometer:micrometer-registry-prometheus:${versions["micrometerRegistryPrometheus"]}")
    implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    //openapi codegen libs
    implementation("jakarta.validation:jakarta.validation-api:${versions["jakartaValidationApi"]}")

    //person-service api
    implementation("com.example:person-service:${versions["personApiVersion"]}")

    //security jwt
    implementation("com.auth0:java-jwt:${versions["javaJwt"]}")
    implementation("com.auth0:jwks-rsa:${versions["jwksRsa"]}")

    //logs
    implementation("net.logstash.logback:logstash-logback-encoder:${versions["logstashLogbackEncoder"]}")
    implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

    //lombok + mapstruct
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.mapstruct:mapstruct:${versions["mapstructVersion"]}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstructVersion"]}")

    //test dependencies
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

/*
──────────────────────────────────────────────────────
============== Api generation ==============
──────────────────────────────────────────────────────
*/

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

/*
──────────────────────────────────────────────────────
============== Resolve NEXUS credentials ==============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach {
    val (k, v) = it.split("=", limit = 2)
    System.setProperty(k.trim(), v.trim())
    logger.lifecycle("${k.trim()}=${v.trim()}")
}

val nexusUrl = System.getenv("LOCAL_NEXUS_URL") ?: System.getProperty("LOCAL_NEXUS_URL")
val nexusUser = System.getenv("LOCAL_NEXUS_USERNAME") ?: System.getProperty("LOCAL_NEXUS_USERNAME")
val nexusPassword = System.getenv("LOCAL_NEXUS_PASSWORD") ?: System.getProperty("LOCAL_NEXUS_PASSWORD")

if (nexusUrl.isNullOrBlank() || nexusUser.isNullOrBlank() || nexusPassword.isNullOrBlank()) {
    throw GradleException(
            "NEXUS_URL or NEXUS_USER or NEXUS_PASSWORD not set. " +
                    "Please create a .env file with these properties or set environment variables."
    )
}

repositories {
    mavenCentral()
    maven {
        url = uri(nexusUrl)
        isAllowInsecureProtocol = true
        credentials {
            username = nexusUser
            password = nexusPassword
        }
    }
}