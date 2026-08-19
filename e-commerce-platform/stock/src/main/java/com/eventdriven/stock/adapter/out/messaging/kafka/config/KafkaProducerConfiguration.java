package com.eventdriven.stock.adapter.out.messaging.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

import static adapter.Constants.DLT;

@Configuration
@RequiredArgsConstructor
class KafkaProducerConfiguration {

    private final KafkaProperties kafkaProperties;
    private final KafkaProducerProperties kafkaProducerProperties;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(kafkaTopicProperties.getStockInitializedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockInitializedTopic() + DLT.getValue()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReplenishedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReplenishedTopic() + DLT.getValue()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReservedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReservedTopic() + DLT.getValue()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReservationFailedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReservationFailedTopic() + DLT.getValue()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReleasedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockReleasedTopic() + DLT.getValue()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockDepletedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getStockDepletedTopic() + DLT.getValue()).partitions(3).replicas(3).build()
        );
    }

    private Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        // the bootstrap is defined in the YAML for each profile, everything else is defined as a bean with the help of shared property class KafkaProducerProperties, which is defined in the base YAML since it is shared across all profiles
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaProducerProperties.getIdempotencyEnabled());
        config.put(ProducerConfig.ACKS_CONFIG, kafkaProducerProperties.getAcks());
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                   kafkaProducerProperties.getMaxInFlightRequestsPerConnection());
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerProperties.getRetries());

        return config;
    }
}
