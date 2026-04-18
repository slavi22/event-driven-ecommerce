package com.eventdriven.apigateway.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Container
    static KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.6")
            .withRealmImportFile("/realm-export.json");

    // we use @DynamicPropertySource since it's the only supported way to set properties for Keycloak according to the docs of testcontainers keycloak
    // => https://github.com/dasniko/testcontainers-keycloak/blob/main/docs/spring-boot.md
    @DynamicPropertySource
    static void registerKeycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                     () -> keycloakContainer.getAuthServerUrl() + "/realms/e-commerce-realm");
    }

    @Test
    @DisplayName("Given no authentication token, when accessing any protected endpoint, then should return 401 Unauthorized")
    void testAnyProtectedEndpoint_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/some/protected/route";

        // Act & Assert
        webTestClient.get().uri(url).exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given an invalid authentication token, when accessing any protected endpoint, then should return 401 Unauthorized")
    void testAnyProtectedEndpoint_withInvalidAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/some/protected/route";
        String invalidToken = "invalid-token";

        // Act & Assert
        webTestClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken).exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given a valid authentication token, when accessing any protected endpoint, then should return any status other than 401")
    void testAnyProtectedEndpoint_withValidAuthenticationToken_shouldReturnOk() throws URISyntaxException {
        // Arrange
        String url = "/some/protected/route";
        URI authorizationURI = new URIBuilder(keycloakContainer.getAuthServerUrl() +
                                              "/realms/e-commerce-realm/protocol/openid-connect/token").build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("e-commerce"));
        formData.put("username", Collections.singletonList("user@gmail.com"));
        formData.put("password", Collections.singletonList("123"));

        String responseBody = webTestClient.post()
                                           .uri(authorizationURI)
                                           .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                           .body(BodyInserters.fromFormData(formData)).exchange()
                                           .expectBody(String.class)
                                           .returnResult()
                                           .getResponseBody();

        var jsonParser = new JacksonJsonParser();
        String validToken = "Bearer " + jsonParser.parseMap(responseBody).get("access_token");


        // Act & Assert
        webTestClient.get().uri(url).header(HttpHeaders.AUTHORIZATION, validToken).exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status); // TODO: change - assert that we don't get 401 Unauthorized, since it will currently fail with 404 Not Found since we don't have any real endpoints yet
                         assertNotEquals(HttpStatus.Series.SERVER_ERROR, HttpStatus.Series.resolve(status)); // assert that we don't get any 500 error due to a runtime exception in the code
                     });

    }

    // TODO: add more tests when i introduce real endpoints that will also require different roles (e.g. admin role for admin endpoints, customer role for customer endpoints, etc.)

}
