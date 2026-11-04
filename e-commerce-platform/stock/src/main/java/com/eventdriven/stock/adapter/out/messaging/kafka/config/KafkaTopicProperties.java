package com.eventdriven.stock.adapter.out.messaging.kafka.config;

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
    private String stockInitializedTopic;
    private String stockReplenishedTopic;
    private String stockReservedTopic;
    private String stockReservationFailedTopic;
    private String stockReleasedTopic;
    private String stockDepletedTopic;
    private String orderPlacedTopic;
    private String orderCancelledTopic;
}
