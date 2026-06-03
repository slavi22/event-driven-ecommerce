package com.eventdriven.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class PaymentTestConfiguration {

    @Bean
    public KafkaAdmin.NewTopics setupTestTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic("order-ready-for-payment-topic", 1, (short) 1),
                new NewTopic("order-ready-for-payment-topic.DLT", 1, (short) 1),
                new NewTopic("payment-processed-topic", 1, (short) 1),
                new NewTopic("payment-processed-topic.DLT", 1, (short) 1),
                new NewTopic("payment-failed-topic", 1, (short) 1),
                new NewTopic("payment-failed-topic.DLT", 1, (short) 1)
        );
    }
}
