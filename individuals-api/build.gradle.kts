import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

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
        "mapstructVersion" to "1.5.5.Final",
        "wiremockTestcontainers" to "1.0-alpha-15",
        "transactionApiVersion" to "1.0.0-SNAPSHOT",
        "javaxAnnotationApiVersion" to "1.3.2",
        "javaxValidationApiVersion" to "2.0.0.Final",
        "swaggerAnnotations" to "2.2.40",
        "currencyRateApiVersion" to "1.0.0-SNAPSHOT",
        "paymentService" to "1.0.0-SNAPSHOT"
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
    implementation("io.swagger.core.v3:swagger-annotations:${versions["swaggerAnnotations"]}")
    implementation("javax.validation:validation-api:${versions["javaxValidationApiVersion"]}")
    implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApiVersion"]}")

    //person-service api
    implementation("com.example:person-service:${versions["personApiVersion"]}")

    //transaction-service api
    implementation("com.example:transaction-service:${versions["transactionApiVersion"]}")

    //currency-rate-service api
    implementation("com.example:currency-rate-service:${versions["currencyRateApiVersion"]}")

    //payment-service api
    implementation("com.example:payment-service:${versions["paymentService"]}")

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
    testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:${versions["wiremockTestcontainers"]}")
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

val openApiDir = file("${rootDir}/openapi")

val foundSpecifications = openApiDir.listFiles { f -> f.extension in listOf("yaml", "yml") } ?: emptyArray()
logger.lifecycle("Found ${foundSpecifications.size} specifications: " + foundSpecifications.joinToString { it.name })

foundSpecifications.forEach { specFile ->
    val ourDir = getAbsolutePath(specFile.nameWithoutExtension)
    val packageName = defineJavaPackageName(specFile.nameWithoutExtension)

    val taskName = buildGenerateApiTaskName(specFile.nameWithoutExtension)
    logger.lifecycle("Register task ${taskName} from ${ourDir.get()}")
    val basePackage = "com.example.${packageName}"

    tasks.register(taskName, GenerateTask::class) {
        generatorName.set("spring")
        inputSpec.set(specFile.absolutePath)
        outputDir.set(ourDir)

        configOptions.set(
                mapOf(
                        "library" to "spring-cloud",
                        "skipDefaultInterface" to "true",
                        "useBeanValidation" to "true",
                        "openApiNullable" to "false",
                        "useTags" to "true",
                        "modelPackage" to "${basePackage}.dto",
                        "configPackage" to "${basePackage}.config",
                )
        )

        globalProperties.set(
                mapOf(
                        "models" to "",
                        "apis" to "false",
                        "supportingFiles" to "false",
                        "modelDocs" to "false"
                )
        )

        doFirst {
            logger.lifecycle("$taskName: starting generation from ${specFile.name}")
        }
    }
}


fun getAbsolutePath(nameWithoutExtension: String): Provider<String> {
    return layout.buildDirectory
            .dir("generated-sources/openapi/${nameWithoutExtension}")
            .map { it.asFile.absolutePath }
}

fun defineJavaPackageName(name: String): String {
    val beforeDash = name.substringBefore('-')
    val match = Regex("^[a-z]+]").find(beforeDash)
    return match?.value ?: beforeDash.lowercase()
}

fun buildGenerateApiTaskName(name: String): String {
    return buildTaskName("generate", name)
}

fun buildJarTaskName(name: String): String {
    return buildTaskName("jar", name)
}

fun buildTaskName(taskPrefix: String, name: String): String {
    val prepareName = name
            .split(Regex("[^A-Za-z0-9]"))
            .filter { it.isNotBlank() }
            .joinToString("") { it.replaceFirstChar(Char::uppercase) }

    return "${taskPrefix}-${prepareName}"
}

val withoutExtensionNames = foundSpecifications.map { it.nameWithoutExtension }

sourceSets.named("main") {
    withoutExtensionNames.forEach { name ->
        java.srcDir(layout.buildDirectory.dir("generated-sources/openapi/$name/src/main/java"))
    }
}

tasks.register("generateAllOpenApi") {
    foundSpecifications.forEach { specFile ->
        dependsOn(buildGenerateApiTaskName(specFile.nameWithoutExtension))
    }
    doLast {
        logger.lifecycle("generateAllOpenApi: all specifications has been generated")
    }
}

tasks.named("compileJava") {
    dependsOn("generateAllOpenApi")
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