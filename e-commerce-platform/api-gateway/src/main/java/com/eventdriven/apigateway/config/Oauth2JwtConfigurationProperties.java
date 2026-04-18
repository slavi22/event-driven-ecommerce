package com.eventdriven.apigateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth2.jwt")
@Getter
@Setter
class Oauth2JwtConfigurationProperties {
    private String clientId;
    private String userIdClaim;
}
