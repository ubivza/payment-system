package com.example.individualsapi.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.List;

public class Container {

    @Value("${keycloak.realm}")
    String realm;
    @Value("${keycloak.client-id}")
    String clientId;

    static KeycloakContainer keycloakTestContainer = KeycloakTestContainerBase.keycloak;
    static WireMockContainer wireMockContainer = WireMockTestContainerBase.wiremockServer;

    public static void start() {
        keycloakTestContainer.start();
        wireMockContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloakTestContainer.getAuthServerUrl() + "/realms/payment-system");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> keycloakTestContainer.getAuthServerUrl() + "/realms/payment-system/protocol/openid-connect/certs");
        registry.add("keycloak.host", keycloakTestContainer::getAuthServerUrl);
        registry.add("keycloak.client-secret", () -> "**********");

        registry.add("person.url", wireMockContainer::getBaseUrl);
    }

    @AfterEach
    void clear() {
        Keycloak adminInstance = getKeycloakInstance();
        List<UserRepresentation> users = adminInstance.realm(realm).users().list();
        for (UserRepresentation user : users) {
            if (!user.getUsername().equals(keycloakTestContainer.getAdminUsername())) {
                adminInstance.realm(realm).users().get(user.getId()).remove();
            }
        }
    }

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakTestContainer.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloakTestContainer.getAdminUsername())
                .password(keycloakTestContainer.getAdminPassword())
                .build();
    }
}
