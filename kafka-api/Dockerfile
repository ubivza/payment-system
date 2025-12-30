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

RUN gradle --no-daemon clean build -x test
RUN gradle publish --info