package com.eventdriven.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class OrderTestConfiguration {

    @Bean
    public KafkaAdmin.NewTopics setupTestTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic("product-created-topic", 1, (short) 1),
                new NewTopic("product-created-topic.DLT", 1, (short) 1),
                new NewTopic("product-updated-topic", 1, (short) 1),
                new NewTopic("product-updated-topic.DLT", 1, (short) 1),
                new NewTopic("stock-reserved-topic", 1, (short) 1),
                new NewTopic("stock-reserved-topic.DLT", 1, (short) 1),
                new NewTopic("stock-reservation-failed-topic", 1, (short) 1),
                new NewTopic("stock-reservation-failed-topic.DLT", 1, (short) 1),
                new NewTopic("stock-released-topic", 1, (short) 1),
                new NewTopic("stock-released-topic.DLT", 1, (short) 1),
                new NewTopic("payment-processed-topic", 1, (short) 1),
                new NewTopic("payment-processed-topic.DLT", 1, (short) 1),
                new NewTopic("payment-failed-topic", 1, (short) 1),
                new NewTopic("payment-failed-topic.DLT", 1, (short) 1),
                new NewTopic("order-placed-topic", 1, (short) 1),
                new NewTopic("order-placed-topic.DLT", 1, (short) 1),
                new NewTopic("order-confirmed-topic", 1, (short) 1),
                new NewTopic("order-cancelled-topic", 1, (short) 1),
                new NewTopic("order-ready-for-payment-topic", 1, (short) 1)
        );
    }
}
