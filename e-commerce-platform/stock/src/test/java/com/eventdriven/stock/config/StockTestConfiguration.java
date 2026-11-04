package com.eventdriven.stock.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class StockTestConfiguration {

    @Bean
    public KafkaAdmin.NewTopics setupTestTopics() {
        return new KafkaAdmin.NewTopics(
                // product-created (consumed by stock)
                new NewTopic("product-created-topic", 1, (short) 1),
                new NewTopic("product-created-topic.DLT", 1, (short) 1),
                // stock-initialized
                new NewTopic("stock-initialized-topic", 1, (short) 1),
                new NewTopic("stock-initialized-topic.DLT", 1, (short) 1),
                // stock-replenished
                new NewTopic("stock-replenished-topic", 1, (short) 1),
                new NewTopic("stock-replenished-topic.DLT", 1, (short) 1),
                // stock-reserved
                new NewTopic("stock-reserved-topic", 1, (short) 1),
                new NewTopic("stock-reserved-topic.DLT", 1, (short) 1),
                // stock-depleted
                new NewTopic("stock-depleted-topic", 1, (short) 1),
                new NewTopic("stock-depleted-topic.DLT", 1, (short) 1)
        );
    }
}
