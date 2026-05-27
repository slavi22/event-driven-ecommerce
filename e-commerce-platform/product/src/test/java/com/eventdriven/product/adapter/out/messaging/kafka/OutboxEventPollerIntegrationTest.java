package com.eventdriven.product.adapter.out.messaging.kafka;

import com.eventdriven.product.adapter.out.persistence.command.postgres.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistence.command.postgres.OutboxEventJpaRepository;
import com.eventdriven.product.domain.event.ProductCreatedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class OutboxEventPollerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.command.jdbc-url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.command.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.command.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.query.jdbc-url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.query.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.query.password", postgreSQLContainer::getPassword);
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

    @BeforeAll
    static void createIntegrationTestTopic() {
        // we need to create the topic manually to essentially override the default topic config we have in place, which is 3 partitions and replication factor of 3
        // solution found from here using Kafka admin client => https://stackoverflow.com/a/59191509
        List<NewTopic> topics = List.of(new NewTopic("product-created-topic", 1, (short) 1));
        Map<String, Object> configMap =
                Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        try (AdminClient adminClient = AdminClient.create(configMap)) {
            adminClient.createTopics(topics);
        }
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
            ConsumerRecords<String, ProductCreatedEventPayload> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
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
            ConsumerRecords<String, ProductCreatedEventPayload> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(2));
            assertEquals(0, records.count());
        }

    }

    private OutboxEventEntity buildEvent(boolean published) {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                productId, "Test Product", "A test product",
                new BigDecimal("9.99"), ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE, Instant.parse("2024-01-01T00:00:00Z")
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
                // essentially the same as => properties = {"spring.json.value.default.type=com.eventdriven.product.domain.event.ProductCreatedEventPayload"} on the real consumer
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ProductCreatedEventPayload.class.getName()
        ));
    }
}
