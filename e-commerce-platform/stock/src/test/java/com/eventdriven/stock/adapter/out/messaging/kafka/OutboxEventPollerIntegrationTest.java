package com.eventdriven.stock.adapter.out.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.stock.config.StockTestConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StockTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OutboxEventPollerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("command");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private OutboxEventPoller outboxEventPoller;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @Autowired
    private JsonMapper jsonMapper;

    @Value("${kafka.topics.stock-initialized-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given unpublished events, when the poller runs, then events should be published and marked as published")
    void testPoller_whenUnpublishedEventsExist_shouldPublishAndMarkAsPublished() {
        // Arrange
        OutboxEventEntity saved = outboxEventJpaRepository.save(buildOutboxEvent(false));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, StockInitializedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, StockInitializedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertEquals(1, records.count());
            ConsumerRecord<String, StockInitializedEventPayload> record = records.iterator().next();
            assertEquals(saved.getAggregateId(), record.key());
        }
        OutboxEventEntity updated = outboxEventJpaRepository.findById(saved.getId()).orElseThrow();
        assertTrue(updated.isPublished());
    }

    @Test
    @DisplayName("Given already published events, when the poller runs, then no events should be re-published")
    void testPoller_whenEventsAlreadyPublished_shouldNotPublishAgain() {
        // Arrange
        outboxEventJpaRepository.save(buildOutboxEvent(true));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, StockInitializedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, StockInitializedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            assertEquals(0, records.count());
        }
    }

    private OutboxEventEntity buildOutboxEvent(boolean published) {
        String stockId = UUID.randomUUID().toString();
        StockInitializedEventPayload payload = new StockInitializedEventPayload(
                stockId, UUID.randomUUID().toString(), 100, Instant.now()
        );
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setAggregateId(stockId);
        entity.setAggregateType(StockInitializedEventPayload.AGGREGATE_TYPE);
        entity.setEventType(StockInitializedEventPayload.EVENT_TYPE);
        entity.setPayload(jsonMapper.writeValueAsString(payload));
        entity.setPublished(published);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private KafkaConsumer<String, StockInitializedEventPayload> buildConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-poller-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, StockInitializedEventPayload.class.getName()
        ));
    }
}
