package com.example.currencyrateservice.config;

import org.testcontainers.junit.jupiter.Container;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class WireMockTestContainerBase {
    @Container
    static WireMockContainer wiremockServer = new WireMockContainer("wiremock/wiremock:3.6.0")
            .withExposedPorts(8080)
            .withMappingFromResource("external-services", "cbr-service-stub.json");
}