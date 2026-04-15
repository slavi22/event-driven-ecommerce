package com.eventdriven.apigateway;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class ApiGatewayApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Container
    //@ServiceConnection
    static KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.6")
            .withRealmImportFile("/realm-export.json");

    @DynamicPropertySource
    static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                     () -> keycloakContainer.getAuthServerUrl() + "/realms/e-commerce-realm");
    }

    @Test
    void testGetEndpoint_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/some/protected/route";

        // Act & Assert
        webTestClient.get().uri(url).exchange().expectStatus().isUnauthorized();
    }

}
