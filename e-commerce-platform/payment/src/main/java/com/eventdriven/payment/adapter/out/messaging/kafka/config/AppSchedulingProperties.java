package com.eventdriven.payment.adapter.out.messaging.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.scheduling")
@Getter
@Setter
class AppSchedulingProperties {

    private boolean enabled;
    private long order;
}
