package com.example.fakepaymentprovider.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class Container {
    static PostgreSQLContainer<?> postgreSQLContainer = PGSQLContainer.postgres;

    public static void start() {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PGSQLContainer.postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PGSQLContainer.postgres.getUsername());
        registry.add("spring.datasource.password", () -> PGSQLContainer.postgres.getPassword());
    }
}