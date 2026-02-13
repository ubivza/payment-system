package com.example.paymentservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class Container {
    static PostgreSQLContainer<?> postgreSQLContainer = PGSQLContainer.postgres;
    static WireMockContainer wireMockContainer = WireMockTestContainerBase.wiremockServer;

    public static void startDBOnly() {
        postgreSQLContainer.start();
    }

    public static void startAll() {
        wireMockContainer.start();
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (wireMockContainer.isRunning()) {
            registry.add("payout.url", wireMockContainer::getBaseUrl);
            registry.add("transaction.url", wireMockContainer::getBaseUrl);
            registry.add("webhook.url", wireMockContainer::getBaseUrl);
        }
        registry.add("spring.datasource.url", () -> PGSQLContainer.postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PGSQLContainer.postgres.getUsername());
        registry.add("spring.datasource.password", () -> PGSQLContainer.postgres.getPassword());
    }
}
