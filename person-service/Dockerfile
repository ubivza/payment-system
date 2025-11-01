FROM gradle:8.14-jdk24 AS builder
ARG NEXUS_URL=http://host.docker.internal:8085/repository/maven-snapshots
ARG NEXUS_USERNAME=admin
ARG NEXUS_PASSWORD=admin

ENV NEXUS_URL=${NEXUS_URL}
ENV NEXUS_USERNAME=${NEXUS_USERNAME}
ENV NEXUS_PASSWORD=${NEXUS_PASSWORD}

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY openapi ./openapi
RUN gradle dependencies --no-daemon

COPY . .
RUN gradle clean build -x test --no-daemon
RUN gradle publish --info

FROM openjdk:24-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]