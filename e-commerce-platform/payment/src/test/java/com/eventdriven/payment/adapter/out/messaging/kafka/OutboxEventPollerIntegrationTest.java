package com.eventdriven.payment.adapter.out.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.payment.config.PaymentTestConfiguration;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = PaymentTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OutboxEventPollerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("payment");

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

    @Value("${kafka.topics.payment-processed-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given an unpublished PaymentProcessed outbox event, when the poller runs, then it should publish the event and mark it as published")
    void testOutbox_whenUnpublishedEvent_shouldPublishAndMarkAsPublished() {
        // Arrange
        String aggregateId = UUID.randomUUID().toString();
        OutboxEventEntity saved = outboxEventJpaRepository.save(buildPaymentProcessedEvent(aggregateId, false));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, PaymentProcessedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, PaymentProcessedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
            assertEquals(1, records.count());
            ConsumerRecord<String, PaymentProcessedEventPayload> record = records.iterator().next();
            assertEquals(aggregateId, record.key());
        }
        assertTrue(outboxEventJpaRepository.findById(saved.getId()).orElseThrow().isPublished());
    }

    @Test
    @DisplayName("Given an already published outbox event, when the poller runs, then it should not publish the event again")
    void testOutbox_whenAlreadyPublishedEvent_shouldNotPublishAgain() {
        // Arrange
        outboxEventJpaRepository.save(buildPaymentProcessedEvent(UUID.randomUUID().toString(), true));

        // Act
        outboxEventPoller.readOutbox();

        // Assert
        try (KafkaConsumer<String, PaymentProcessedEventPayload> consumer = buildConsumer()) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, PaymentProcessedEventPayload> records =
                    KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            assertEquals(0, records.count());
        }
    }

    private OutboxEventEntity buildPaymentProcessedEvent(String aggregateId, boolean published) {
        PaymentProcessedEventPayload payload = new PaymentProcessedEventPayload(
                UUID.randomUUID().toString(), new BigDecimal("100.00"), Instant.now());
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setAggregateId(aggregateId);
        entity.setAggregateType(PaymentProcessedEventPayload.AGGREGATE_TYPE);
        entity.setEventType(PaymentProcessedEventPayload.EVENT_TYPE);
        entity.setPayload(jsonMapper.writeValueAsString(payload));
        entity.setPublished(published);
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private KafkaConsumer<String, PaymentProcessedEventPayload> buildConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentProcessedEventPayload.class.getName()
        ));
    }
}
