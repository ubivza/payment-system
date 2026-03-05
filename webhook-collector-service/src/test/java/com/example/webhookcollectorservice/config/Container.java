package com.example.webhookcollectorservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;

public class Container {
    static KafkaContainer kafkaContainer = KafkaTestContainer.kafka;
    static PostgreSQLContainer postgreSQLContainer = PGSQLContainer.postgres;

    public static void start() {
        kafkaContainer.start();
        postgreSQLContainer.start();
    }


    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PGSQLContainer.postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PGSQLContainer.postgres.getUsername());
        registry.add("spring.datasource.password", () -> PGSQLContainer.postgres.getPassword());

        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }
}
