package com.example.transactionservice.config;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class PGSQLContainer {
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer("postgres:latest")
            .withUsername("admin")
            .withPassword("admin")
            .withDatabaseName("test");
}
