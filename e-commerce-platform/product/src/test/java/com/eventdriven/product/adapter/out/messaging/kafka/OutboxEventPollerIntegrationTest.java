package com.eventdriven.product.adapter.out.messaging.kafka;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.product.config.ProductTestConfiguration;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OutboxEventPollerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // we need dynamically property source, since if i @ServiceConnection it won't pick up our topics defined in the ProductTestConfiguration
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private OutboxEventPoller outboxEventPoller;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @Autowired
    private JsonMapper jsonMapper;
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
        try (KafkaConsumer<String, ProductCreatedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, ProductCreatedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertEquals(1, records.count());
            ConsumerRecord<String, ProductCreatedEventPayload> consumerRecord = records.iterator().next();
            assertEquals(savedEntity.getAggregateId(), consumerRecord.key());
            assertEquals("Test Product", consumerRecord.value().name());
        }
        OutboxEventEntity updated = outboxEventJpaRepository.findById(savedEntity.getId()).orElseThrow();
        assertTrue(updated.isPublished());
    }

    @Test
    @DisplayName("Given there are already published events in the outbox, when the poller runs, then it should not publish the events again")
    void testOutbox_whenThereAreAlreadyPublishedEvents_shouldNotPublishAgain() {
        // Arrange
        outboxEventJpaRepository.save(buildEvent(true));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, ProductCreatedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, ProductCreatedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            assertEquals(0, records.count());
        }

    }

    private OutboxEventEntity buildEvent(boolean published) {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                productId, "Test Product", "A test product",
                new BigDecimal("9.99"), ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE, 10, Instant.now()
        );
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateId(UUID.randomUUID().toString());
        event.setAggregateType("Product");
        event.setEventType("ProductCreated");
        event.setPayload(jsonMapper.writeValueAsString(payload));
        event.setPublished(published);
        event.setCreatedAt(Instant.now());
        return event;
    }

    private KafkaConsumer<String, ProductCreatedEventPayload> buildConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                // essentially the same as => properties = {"spring.json.value.default.type=product.event.ProductCreatedEventPayload"} on the real consumer
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ProductCreatedEventPayload.class.getName()
        ));
    }
}
