package com.eventdriven.product.adapter.out.messaging.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

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
                TopicBuilder.name(kafkaTopicProperties.getProductCreatedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getProductCreatedTopic() + ".DLT").partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getProductUpdatedTopic()).partitions(3).replicas(3).build(),
                TopicBuilder.name(kafkaTopicProperties.getProductUpdatedTopic() + ".DLT").partitions(3).replicas(3).build()
        );
    }

    // DLT setup
    @Bean
    public DefaultErrorHandler errorHandler() {
        // We cannot reuse the main KafkaTemplate<String, String> here because by the time the
        // DeadLetterPublishingRecoverer kicks in, deserialization has already happened — the ConsumerRecord
        // holds a ProductCreatedEventPayload object, not the original bytes. StringSerializer only handles
        // String values, so passing it a domain object causes a SerializationException.
        // JacksonJsonSerializer can serialize any Java object to JSON bytes, which is what we need.
        Map<String, Object> dltConfig = new HashMap<>(producerConfig());
        dltConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        KafkaTemplate<String, Object> dltKafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(dltConfig));
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(dltKafkaTemplate, ((consumerRecord, _) -> new TopicPartition(
                        consumerRecord.topic() + ".DLT", -1)));
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        return new DefaultErrorHandler(recoverer, backOff);
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
