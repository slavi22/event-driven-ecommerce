package com.eventdriven.order.adapter.out.messaging.kafka.config;

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
    private String stockReservedTopic;
    private String stockReservationFailedTopic;
    private String stockReleasedTopic;
    private String paymentProcessedTopic;
    private String paymentFailedTopic;
    private String orderPlacedTopic;
    private String orderConfirmedTopic;
    private String orderCancelledTopic;
    private String orderReadyForPaymentTopic;
}
