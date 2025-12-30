import org.gradle.api.publish.maven.MavenPublication

plugins {
    java
    id("maven-publish")
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
description = "kafka-api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

configurations.all { resolutionStrategy.cacheChangingModulesFor(0, "seconds") }

repositories {
    mavenCentral()
}

dependencies {
    //lombok
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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
        create<MavenPublication>("publish${name.replaceFirstChar(Char::uppercase)}Jar") {
            from(components["java"])
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
