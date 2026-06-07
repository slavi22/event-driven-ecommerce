package com.eventdriven.apigateway.apidocs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
class ApiDocsSecurityInjectionFilter {

    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private static final String SECURITY_SCHEME_NAME = "Keycloak-OAuth";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    GatewayFilter apply() {
        var config = new ModifyResponseBodyGatewayFilterFactory.Config();
        config.setInClass(String.class);
        config.setOutClass(String.class);
        config.setRewriteFunction(String.class, String.class, (_, body) -> {
            try {
                return Mono.just(injectSecurity(body));
            } catch (Exception _) {
                return Mono.just(body);
            }
        });
        return modifyResponseBodyFilter.apply(config);
    }

    private String injectSecurity(String body) throws JsonProcessingException {
        ObjectNode root = (ObjectNode) MAPPER.readTree(body);

        ObjectNode components = root.has("components")
                ? (ObjectNode) root.get("components")
                : MAPPER.createObjectNode();

        ObjectNode authCodeFlow = MAPPER.createObjectNode();
        authCodeFlow.put("authorizationUrl", authServerUrl + "/realms/" + realm + "/protocol/openid-connect/auth");
        authCodeFlow.put("tokenUrl", authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token");
        authCodeFlow.set("scopes", MAPPER.createObjectNode());

        ObjectNode flows = MAPPER.createObjectNode();
        flows.set("authorizationCode", authCodeFlow);

        ObjectNode scheme = MAPPER.createObjectNode();
        scheme.put("type", "oauth2");
        scheme.set("flows", flows);

        ObjectNode securitySchemes = MAPPER.createObjectNode();
        securitySchemes.set(SECURITY_SCHEME_NAME, scheme);
        components.set("securitySchemes", securitySchemes);
        root.set("components", components);

        ObjectNode secReq = MAPPER.createObjectNode();
        secReq.set(SECURITY_SCHEME_NAME, MAPPER.createArrayNode());
        ArrayNode security = MAPPER.createArrayNode();
        security.add(secReq);
        root.set("security", security);

        ObjectNode server = MAPPER.createObjectNode();
        server.put("url", gatewayUrl);
        ArrayNode servers = MAPPER.createArrayNode();
        servers.add(server);
        root.set("servers", servers);

        return MAPPER.writeValueAsString(root);
    }
}
