package com.example.fakepaymentprovider.config;

import org.testcontainers.junit.jupiter.Container;
import org.wiremock.integrations.testcontainers.WireMockContainer;

public class WireMockTestContainerBase {

    @Container
    static WireMockContainer wiremock =
            new WireMockContainer("wiremock/wiremock:3.6.0")
                    .withExposedPorts(8080)
                    .withMappingFromResource(
                            "webhooks",
                            "webhook-stub.json"
                    );
}
