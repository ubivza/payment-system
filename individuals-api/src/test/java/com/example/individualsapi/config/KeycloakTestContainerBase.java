package com.example.individualsapi.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

public class KeycloakTestContainerBase {
    @Container
    public static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.5")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withRealmImportFile("realm-config.json");
}