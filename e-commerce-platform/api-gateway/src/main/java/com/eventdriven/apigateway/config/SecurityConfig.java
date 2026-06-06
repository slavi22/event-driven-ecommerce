package com.eventdriven.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, JwtConverter jwtConverter) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize
                        // Allow unauthenticated access to Swagger UI and API docs
                        .pathMatchers("swagger-ui/**", "swagger-ui**", "/v3/api-docs/**", "/v3/api-docs**").permitAll()
                        // Anyone can browse products without logging in
                        .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/**").permitAll()
                        // Product writes are admin-only
                        .pathMatchers(HttpMethod.POST, "/api/v1/products").hasRole("admin")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/products").hasRole("admin")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("admin")
                        // Stock replenishment is admin-only
                        .pathMatchers(HttpMethod.POST, "/api/v1/stocks/**").hasRole("admin")
                        // Everything else (orders, stock reads, payments) requires the customer role
                        .anyExchange().hasRole("customer"))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                        new ReactiveJwtAuthenticationConverterAdapter(jwtConverter))));

        return http.build();
    }
}
