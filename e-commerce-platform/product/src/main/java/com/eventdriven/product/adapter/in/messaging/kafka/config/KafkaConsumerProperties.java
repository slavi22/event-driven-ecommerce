package com.eventdriven.product.adapter.in.messaging.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.config.consumer")
@Getter
@Setter
class KafkaConsumerProperties {

    private Groups groups = new Groups();
    private String autoOffset;
    private String jsonDeserializerTrustedPackages;

    @Getter
    @Setter
    public static class Groups {
        private String productCreatedEventsGroup;
        private String productUpdatedEventsGroup;
    }
}
