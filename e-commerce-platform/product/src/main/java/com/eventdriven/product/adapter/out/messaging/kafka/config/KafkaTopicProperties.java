package com.eventdriven.product.adapter.out.messaging.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.topics")
@Getter
@Setter
public class KafkaTopicProperties {
    private String productCreatedTopic;
    private String productUpdatedTopic;
    private String productDeletedTopic;
}
