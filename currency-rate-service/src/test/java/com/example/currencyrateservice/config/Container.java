package com.example.currencyrateservice.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class Container {
    static WireMockContainer wireMockContainer = WireMockTestContainerBase.wiremockServer;
    static PostgreSQLContainer<?> postgreSQLContainer = PGSQLContainer.postgres;

    public static void start() {
        postgreSQLContainer.start();
        wireMockContainer.start();
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PGSQLContainer.postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PGSQLContainer.postgres.getUsername());
        registry.add("spring.datasource.password", () -> PGSQLContainer.postgres.getPassword());

        registry.add("rate.provider.cbr.url", () -> wireMockContainer.getBaseUrl());
    }
}