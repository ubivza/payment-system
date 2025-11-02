package com.example.personservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class PGSQLContainer {

    @Container
    static PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer("postgres:latest")
                .withUsername("admin")
                .withPassword("admin")
                .withDatabaseName("test");

        postgres.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }
}
