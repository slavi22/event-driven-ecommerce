package com.eventdriven.product.adapter.out.messaging.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.config.producer")
@Getter
@Setter
class KafkaProducerProperties {

    private Boolean idempotencyEnabled;
    private String acks;
    private Integer maxInFlightRequestsPerConnection;
    private Integer retries;
}
