package com.example.transactionservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;

public class Container {
    static KafkaContainer kafkaContainer = KafkaTestContainer.kafka;

    public static void start() {
        PGSQLContainer.postgres0.start();
        PGSQLContainer.postgres1.start();
        kafkaContainer.start();
    }

    public static void startDatabases() {
        PGSQLContainer.postgres0.start();
        PGSQLContainer.postgres1.start();
    }

    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:shardingsphere:classpath:test-sharding.yml");
        registry.add("flyway.dataSources[0].url", () -> "jdbc:postgresql://localhost:65431/test");
        registry.add("flyway.dataSources[0].username", () -> "admin");
        registry.add("flyway.dataSources[0].password", () -> "admin");
        registry.add("flyway.dataSources[1].url", () -> "jdbc:postgresql://localhost:65432/test");
        registry.add("flyway.dataSources[1].username", () -> "admin");
        registry.add("flyway.dataSources[1].password", () -> "admin");

        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }
}
