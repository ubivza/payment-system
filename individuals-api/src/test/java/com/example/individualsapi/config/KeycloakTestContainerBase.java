package com.example.individualsapi.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

@TestConfiguration
@Testcontainers
@SpringBootTest
public abstract class KeycloakTestContainerBase {
    @Value("${keycloak.realm}")
    String realm;
    @Value("${keycloak.client-id}")
    String clientId;

    @Container
    public static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.5")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withRealmImportFile("realm-config.json");

    static {
        keycloak.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloak.getAuthServerUrl() + "/realms/payment-system/protocol/openid-connect/certs");
        registry.add("keycloak.host", keycloak::getAuthServerUrl);
        registry.add("keycloak.client-secret", () -> "**********");
    }

    @AfterEach
    void clear() {
        Keycloak adminInstance = getKeycloakInstance();
        List<UserRepresentation> users = adminInstance.realm(realm).users().list();
        for (UserRepresentation user : users) {
            if (!user.getUsername().equals(keycloak.getAdminUsername())) {
                adminInstance.realm(realm).users().get(user.getId()).remove();
            }
        }
    }

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();
    }
}