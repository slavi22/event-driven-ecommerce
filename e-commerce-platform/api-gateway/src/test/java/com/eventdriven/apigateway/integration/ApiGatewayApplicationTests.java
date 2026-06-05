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
@AutoConfigureWebTestClient(timeout = "36000") // need the timeout, otherwise we will get => "java.lang.IllegalStateException: Timeout on blocking read for 5000000000 NANOSECONDS"
@Testcontainers
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    // TODO: check everything

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
                     () -> keycloakContainer.getAuthServerUrl() + "/realms/e-commerce");
    }

    // Generic auth tests

    @Test
    @DisplayName("Given no authentication token, when accessing any protected endpoint, then should return 401 Unauthorized")
    void testAnyProtectedEndpoint_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/some/protected/route";

        // Act & Assert
        webTestClient.get().uri(url).exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given an invalid authentication token, when accessing any protected endpoint, then should return 401 Unauthorized")
    void testAnyProtectedEndpoint_withInvalidAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/some/protected/route";

        // Act & Assert
        webTestClient.get().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                     .exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given a valid authentication token, when accessing any protected endpoint, then should return any status other than 401")
    void testAnyProtectedEndpoint_withValidAuthenticationToken_shouldReturnOk() throws URISyntaxException {
        // Arrange
        String url = "/some/protected/route";
        String token = obtainToken("user@gmail.com", "123");

        // Act & Assert
        // /some/protected/route has no matching gateway route so the response will be 404,
        // which still confirms the request passed security (not 401).
        webTestClient.get().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.Series.SERVER_ERROR, HttpStatus.Series.resolve(status));
                     });
    }

    // Public product browsing

    @Test
    @DisplayName("Given no authentication token, when browsing products, then should return any status other than 401")
    void testGetProducts_withNoAuthenticationToken_shouldNotReturnUnauthorized() {
        // Arrange
        String url = "/api/v1/products";

        // Act & Assert
        webTestClient.get().uri(url).exchange()
                     .expectStatus()
                     .value(status -> assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status));
    }

    @Test
    @DisplayName("Given a customer token, when browsing products, then should return any status other than 401 or 403")
    void testGetProducts_withCustomerToken_shouldNotReturnUnauthorizedOrForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/products";
        String token = obtainToken("user@gmail.com", "123");

        // Act & Assert
        webTestClient.get().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                     });
    }

    // Admin-only product write endpoints

    @Test
    @DisplayName("Given no authentication token, when creating a product, then should return 401 Unauthorized")
    void testCreateProduct_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/api/v1/products";

        // Act & Assert
        webTestClient.post().uri(url).exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given a customer token, when creating a product, then should return 403 Forbidden")
    void testCreateProduct_withCustomerToken_shouldReturnForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/products";
        String token = obtainToken("user@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Given an admin token, when creating a product, then should return any status other than 401 or 403")
    void testCreateProduct_withAdminToken_shouldNotReturnUnauthorizedOrForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/products";
        String token = obtainToken("admin@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                     });
    }

    // Customer endpoints

    @Test
    @DisplayName("Given no authentication token, when placing an order, then should return 401 Unauthorized")
    void testPlaceOrder_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/api/v1/orders";

        // Act & Assert
        webTestClient.post().uri(url).exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given a customer token, when placing an order, then should return any status other than 401 or 403")
    void testPlaceOrder_withCustomerToken_shouldNotReturnUnauthorizedOrForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/orders";
        String token = obtainToken("user@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                     });
    }

    @Test
    @DisplayName("Given an admin token (who also holds the customer role), when placing an order, then should return any status other than 401 or 403")
    void testPlaceOrder_withAdminToken_shouldNotReturnUnauthorizedOrForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/orders";
        String token = obtainToken("admin@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                     });
    }

    // Admin-only stock replenishment

    @Test
    @DisplayName("Given no authentication token, when replenishing stock, then should return 401 Unauthorized")
    void testReplenishStock_withNoAuthenticationToken_shouldReturnUnauthorized() {
        // Arrange
        String url = "/api/v1/stocks/some-product-id/replenish";

        // Act & Assert
        webTestClient.post().uri(url).exchange()
                     .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Given a customer token, when replenishing stock, then should return 403 Forbidden")
    void testReplenishStock_withCustomerToken_shouldReturnForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/stocks/some-product-id/replenish";
        String token = obtainToken("user@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Given an admin token, when replenishing stock, then should return any status other than 401 or 403")
    void testReplenishStock_withAdminToken_shouldNotReturnUnauthorizedOrForbidden() throws URISyntaxException {
        // Arrange
        String url = "/api/v1/stocks/some-product-id/replenish";
        String token = obtainToken("admin@gmail.com", "123");

        // Act & Assert
        webTestClient.post().uri(url)
                     .header(HttpHeaders.AUTHORIZATION, token)
                     .exchange()
                     .expectStatus()
                     .value(status -> {
                         assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                         assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                     });
    }

    private String obtainToken(String username, String password) throws URISyntaxException {
        URI tokenUri = new URIBuilder(
                keycloakContainer.getAuthServerUrl() + "/realms/e-commerce/protocol/openid-connect/token").build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("e-commerce"));
        formData.put("username", Collections.singletonList(username));
        formData.put("password", Collections.singletonList(password));

        String responseBody = webTestClient.post()
                                           .uri(tokenUri)
                                           .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                           .body(BodyInserters.fromFormData(formData))
                                           .exchange()
                                           .expectBody(String.class)
                                           .returnResult()
                                           .getResponseBody();

        var jsonParser = new JacksonJsonParser();
        return "Bearer " + jsonParser.parseMap(responseBody).get("access_token");
    }
}
