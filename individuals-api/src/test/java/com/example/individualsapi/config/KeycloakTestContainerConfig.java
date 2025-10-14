package com.example.individualsapi.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@TestConfiguration
@Testcontainers
public class KeycloakTestContainerConfig {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.5")
            .withStartupTimeout(Duration.ofSeconds(180))
            .withRealmImportFile("realm-config.json");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system/protocol/openid-connect/certs");
        registry.add("keycloak.host", keycloak::getAuthServerUrl);
        registry.add("keycloak.client-secret", () -> "**********");
    }
}