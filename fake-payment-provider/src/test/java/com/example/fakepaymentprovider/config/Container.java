package com.example.fakepaymentprovider.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class Container {
    static PostgreSQLContainer<?> postgreSQLContainer = PGSQLContainer.postgres;
    static WireMockContainer wireMockContainer = WireMockTestContainerBase.wiremock;

    public static void start() {
        postgreSQLContainer.start();
    }

    public static void startAll() {
        wireMockContainer.start();
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        if (wireMockContainer.isRunning()) {
            registry.add(
                    "webhook.outgoing-url",
                    () -> wireMockContainer.getBaseUrl() + "/webhook"
            );
            registry.add("webhook.scheduler.fixed-delay-ms", () -> "9999999");
        }
        registry.add("spring.datasource.url", () -> PGSQLContainer.postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PGSQLContainer.postgres.getUsername());
        registry.add("spring.datasource.password", () -> PGSQLContainer.postgres.getPassword());
    }
}