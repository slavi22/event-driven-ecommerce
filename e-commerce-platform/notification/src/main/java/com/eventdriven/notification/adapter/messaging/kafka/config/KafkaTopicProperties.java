package com.eventdriven.notification.adapter.messaging.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.topics")
@Getter
@Setter
class KafkaTopicProperties {
    private String orderPlacedTopic;
    private String orderConfirmedTopic;
    private String orderCancelledTopic;
    private String paymentProcessedTopic;
    private String paymentFailedTopic;
}
