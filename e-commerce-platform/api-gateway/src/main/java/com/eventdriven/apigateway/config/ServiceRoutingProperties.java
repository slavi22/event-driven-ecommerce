package com.eventdriven.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
class ServiceRoutingProperties {
    private String productCommandUri;
    private String orderCommandUri;
    private String stockCommandUri;
    private String projectionUri;
}
