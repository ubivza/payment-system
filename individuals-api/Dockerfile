FROM gradle:8.14-jdk24 AS builder
ARG LOCAL_NEXUS_URL=http://host.docker.internal:8085/repository/maven-snapshots
ARG LOCAL_NEXUS_USERNAME=admin
ARG LOCAL_NEXUS_PASSWORD=admin

ENV LOCAL_NEXUS_URL=${LOCAL_NEXUS_URL}
ENV LOCAL_NEXUS_USERNAME=${LOCAL_NEXUS_USERNAME}
ENV LOCAL_NEXUS_PASSWORD=${LOCAL_NEXUS_PASSWORD}

WORKDIR /home/gradle/project

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src
COPY openapi ./openapi

RUN gradle --no-daemon clean bootJar

FROM openjdk:24-ea-17-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]