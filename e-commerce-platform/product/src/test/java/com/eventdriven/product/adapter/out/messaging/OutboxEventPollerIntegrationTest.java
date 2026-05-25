package com.eventdriven.product.adapter.out.messaging;

import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class OutboxEventPollerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    @ServiceConnection
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @Autowired
    private OutboxEventPoller outboxEventPoller;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @Value("${kafka.topics.product-created-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given there are unpublished events in the outbox, when the poller runs, then it should publish the events and mark them as published")
    void testOutbox_whenThereAreUnpublishedEvents_shouldPublishAndMarkAsPublished() {
        // Arrange
        OutboxEventEntity savedEntity = outboxEventJpaRepository.save(buildEvent(false));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, String> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertEquals(1, records.count());
            ConsumerRecord<String, String> consumerRecord = records.iterator().next();
            assertEquals(savedEntity.getAggregateId(), consumerRecord.key());
            assertEquals(savedEntity.getPayload(), consumerRecord.value());
        }
    }

    @Test
    @DisplayName("Given there are already published events in the outbox, when the poller runs, then it should not publish the events again")
    void testOutbox_whenThereAreAlreadyPublishedEvents_shouldNotPublishAgain() {
        // Arrange
        outboxEventJpaRepository.save(buildEvent(true));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, String> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            assertEquals(0, records.count());
        }

    }

    private OutboxEventEntity buildEvent(boolean published) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateId(UUID.randomUUID().toString());
        event.setAggregateType("Product");
        event.setEventType("ProductCreated");
        event.setPayload("{\"id\":\"123\"}");
        event.setPublished(published);
        event.setCreatedAt(Instant.now());
        return event;
    }

    private KafkaConsumer<String, String> buildConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()
        ));
    }
}
