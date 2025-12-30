import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.api.publish.maven.MavenPublication

plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("org.openapi.generator") version "7.13.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
description = "transaction-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

configurations.all { resolutionStrategy.cacheChangingModulesFor(0, "seconds") }

val versions = mapOf(
        "mapstructVersion" to "1.5.5.Final",
        "javaxAnnotationApiVersion" to "1.3.2",
        "javaxValidationApiVersion" to "2.0.0.Final",
        "springCloudStarterOpenfeign" to "4.1.1",
        "logbackClassicVersion" to "1.5.18",
        "testcontainers" to "1.21.3",
        "logstashLogbackEncoder" to "8.1",
        "micrometerRegistryPrometheus" to "1.15.4",
        "junitJupiter" to "1.21.3",
        "feignMicrometerVersion" to "13.6",
        "hibernateEnvers" to "6.4.4.Final",
        "apacheCommons" to "3.19.0",
        "swaggerAnnotations" to "2.2.40",
        "springKafka" to "3.3.1",
        "javaJwtApi" to "0.13.0",
        "javaJwtImpl" to "0.13.0",
        "javaJwtJackson" to "0.13.0",
        "shardingSphere" to "5.5.2",
        "kafkaApi" to "1.0.0-SNAPSHOT",
)

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
    }
}

dependencies {
    //starters
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
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

    //kafka api
    implementation("com.example:kafka-api:${versions["kafkaApi"]}")

    //utils
    implementation("org.apache.commons:commons-lang3:${versions["apacheCommons"]}")

    //kafka
    implementation("org.springframework.kafka:spring-kafka:${versions["springKafka"]}")

    //jwt
    implementation("io.jsonwebtoken:jjwt-api:${versions["javaJwtApi"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${versions["javaJwtImpl"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${versions["javaJwtJackson"]}")

    //db
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.apache.shardingsphere:shardingsphere-jdbc:${versions["shardingSphere"]}")

    //openapi codegen libs
    implementation("io.swagger.core.v3:swagger-annotations:${versions["swaggerAnnotations"]}")
    implementation("javax.validation:validation-api:${versions["javaxValidationApiVersion"]}")
    implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApiVersion"]}")

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
    testImplementation("org.testcontainers:testcontainers:${versions["testcontainers"]}")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:junit-jupiter:${versions["junitJupiter"]}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
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
                        "useFeignClientUrl" to "true",
                        "useTags" to "true",
                        "apiPackage" to "${basePackage}.api",
                        "modelPackage" to "${basePackage}.dto",
                        "configPackage" to "${basePackage}.config",
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
============== Building jars ==============
──────────────────────────────────────────────────────
*/

tasks.named("build") {
    dependsOn(generatedJars)
}

val generatedJars = foundSpecifications.map { specFile ->
    val name = specFile.nameWithoutExtension
    val generateTaskName = buildGenerateApiTaskName(name)
    val jarTaskName = buildJarTaskName(name)
    val outDirProvider = getAbsolutePath(name)
    val generateSrcDir = outDirProvider.map { File(it).resolve("src/main/java") }

    val sourcesSetName = name

    val sourceSet = sourceSets.create(sourcesSetName) {
        java.srcDir(generateSrcDir)
        compileClasspath += sourceSets["main"].compileClasspath
    }

    val compileTaskName = "compile${sourcesSetName.replaceFirstChar(Char::uppercase)}Java"
    tasks.register<JavaCompile>(compileTaskName) {
        source = sourceSet.java
        classpath = sourceSet.compileClasspath
        destinationDirectory.set(layout.buildDirectory.dir("classes/${sourcesSetName}"))
        dependsOn(generateTaskName)
    }

    tasks.register<Jar>(jarTaskName) {
        group = "build"
        archiveBaseName.set(name)
        destinationDirectory.set(layout.buildDirectory.dir("libs"))

        val classOutput = layout.buildDirectory.dir("classes/${sourcesSetName}")
        from(classOutput)
        dependsOn(compileTaskName)

        doFirst {
            println("Building JAR for $name from compiled classes in ${classOutput.get().asFile}")
        }
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
            "NEXUS details are not set. Create a .env file with correct properties: " +
                    "LOCAL_NEXUS_URL, LOCAL_NEXUS_USERNAME, LOCAL_NEXUS_PASSWORD"
    )
}

/*
──────────────────────────────────────────────────────
============== Nexus Publishing ==============
──────────────────────────────────────────────────────
*/

publishing {
    publications {
        foundSpecifications.forEach { specFile ->
            val name = specFile.nameWithoutExtension
            val jarBaseName = name
            var jarFile = file("build/libs")
                    .listFiles()
                    ?.firstOrNull { it.name.contains(name) && (it.extension == "jar" || it.extension == "zip") }

            if (jarFile != null) {
                logger.lifecycle("publishing: ${jarFile.name}")

                create<MavenPublication>("publish${name.replaceFirstChar(Char::uppercase)}Jar") {
                    artifact(jarFile)
                    groupId = "com.example"
                    artifactId = jarBaseName
                    version = "1.0.0-SNAPSHOT"

                    pom {
                        this.name.set("Generated API $jarBaseName")
                        this.description.set("OpenAPI generated code for $jarBaseName")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "nexus"
            url = uri(nexusUrl)
            isAllowInsecureProtocol = true
            credentials {
                username = nexusUser
                password = nexusPassword
            }
        }
    }
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
