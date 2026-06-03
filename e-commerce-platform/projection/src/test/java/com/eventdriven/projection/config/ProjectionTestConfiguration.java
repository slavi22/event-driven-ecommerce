package com.eventdriven.projection.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class ProjectionTestConfiguration {

    @Bean
    public KafkaAdmin.NewTopics setupTestTopics() {
        return new KafkaAdmin.NewTopics(
                new NewTopic("product-created-topic", 1, (short) 1),
                new NewTopic("product-created-topic.DLT", 1, (short) 1),
                new NewTopic("product-updated-topic", 1, (short) 1),
                new NewTopic("product-updated-topic.DLT", 1, (short) 1),
                new NewTopic("product-deleted-topic", 1, (short) 1),
                new NewTopic("product-deleted-topic.DLT", 1, (short) 1),
                new NewTopic("stock-initialized-topic", 1, (short) 1),
                new NewTopic("stock-initialized-topic.DLT", 1, (short) 1),
                new NewTopic("stock-replenished-topic", 1, (short) 1),
                new NewTopic("stock-replenished-topic.DLT", 1, (short) 1),
                new NewTopic("order-placed-topic", 1, (short) 1),
                new NewTopic("order-placed-topic.DLT", 1, (short) 1),
                new NewTopic("order-confirmed-topic", 1, (short) 1),
                new NewTopic("order-confirmed-topic.DLT", 1, (short) 1),
                new NewTopic("order-cancelled-topic", 1, (short) 1),
                new NewTopic("order-cancelled-topic.DLT", 1, (short) 1)
        );
    }
}
