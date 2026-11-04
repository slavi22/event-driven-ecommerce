package com.eventdriven.apigateway.apidocs;

import com.eventdriven.apigateway.routing.ServiceRoutingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
class ApiDocsRoutesConfig {

    private final ServiceRoutingProperties serviceRoutingProperties;
    private final ApiDocsSecurityInjectionFilter apiDocsSecurityInjectionFilter;

    @Bean
    public RouteLocator apiDocsRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
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
                .build();
    }
}
