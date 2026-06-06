package com.eventdriven.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
class GatewayConfig {

    private final ServiceRoutingProperties serviceRoutingProperties;
    private final ApiDocsSecurityInjectionFilter apiDocsSecurityInjectionFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // API docs proxy routes — inject Keycloak security scheme into downstream specs
                .route("product-api-docs", r -> r
                        .path("/v3/api-docs/product")
                        .filters(f -> f
                                .rewritePath("/v3/api-docs/product", "/v3/api-docs")
                                .filter(apiDocsSecurityInjectionFilter.apply()))
                        .uri(serviceRoutingProperties.getProductCommandUri()))
                .route("order-api-docs", r -> r
                        .path("/v3/api-docs/order")
                        .filters(f -> f
                                .rewritePath("/v3/api-docs/order", "/v3/api-docs")
                                .filter(apiDocsSecurityInjectionFilter.apply()))
                        .uri(serviceRoutingProperties.getOrderCommandUri()))
                .route("stock-api-docs", r -> r
                        .path("/v3/api-docs/stock")
                        .filters(f -> f
                                .rewritePath("/v3/api-docs/stock", "/v3/api-docs")
                                .filter(apiDocsSecurityInjectionFilter.apply()))
                        .uri(serviceRoutingProperties.getStockCommandUri()))
                .route("projection-api-docs", r -> r
                        .path("/v3/api-docs/projection")
                        .filters(f -> f
                                .rewritePath("/v3/api-docs/projection", "/v3/api-docs")
                                .filter(apiDocsSecurityInjectionFilter.apply()))
                        .uri(serviceRoutingProperties.getProjectionUri()))
                // Product commands (POST create, PUT update, DELETE) => product service
                .route("product-command", r -> r
                        .path("/api/v1/products", "/api/v1/products/**")
                        .and().method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .uri(serviceRoutingProperties.getProductCommandUri()))
                // Product queries (GET) => projection service
                .route("product-query", r -> r
                        .path("/api/v1/products", "/api/v1/products/**")
                        .and().method(HttpMethod.GET)
                        .uri(serviceRoutingProperties.getProjectionUri()))
                // Order commands (POST place order) => order service
                .route("order-command", r -> r
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .and().method(HttpMethod.POST)
                        .uri(serviceRoutingProperties.getOrderCommandUri()))
                // Order queries (GET) => projection service
                .route("order-query", r -> r
                        .path("/api/v1/orders", "/api/v1/orders/**")
                        .and().method(HttpMethod.GET)
                        .uri(serviceRoutingProperties.getProjectionUri()))
                // Stock commands (POST replenish) => stock service
                .route("stock-command", r -> r
                        .path("/api/v1/stocks/**")
                        .and().method(HttpMethod.POST)
                        .uri(serviceRoutingProperties.getStockCommandUri()))
                // Stock queries (GET) => projection service
                .route("stock-query", r -> r
                        .path("/api/v1/stocks/**")
                        .and().method(HttpMethod.GET)
                        .uri(serviceRoutingProperties.getProjectionUri()))
                // Payment queries => projection service
                .route("payment-query", r -> r
                        .path("/api/v1/payments/**")
                        .uri(serviceRoutingProperties.getProjectionUri()))
                .build();
    }
}
