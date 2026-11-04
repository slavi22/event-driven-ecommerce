package com.eventdriven.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class NotificationTestConfiguration {

    @Bean
    public KafkaAdmin.NewTopics setupTestTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic("order-placed-topic", 1, (short) 1),
                new NewTopic("order-placed-topic.DLT", 1, (short) 1),
                new NewTopic("order-confirmed-topic", 1, (short) 1),
                new NewTopic("order-confirmed-topic.DLT", 1, (short) 1),
                new NewTopic("order-cancelled-topic", 1, (short) 1),
                new NewTopic("order-cancelled-topic.DLT", 1, (short) 1),
                new NewTopic("payment-processed-topic", 1, (short) 1),
                new NewTopic("payment-processed-topic.DLT", 1, (short) 1),
                new NewTopic("payment-failed-topic", 1, (short) 1),
                new NewTopic("payment-failed-topic.DLT", 1, (short) 1)
        );
    }
}
