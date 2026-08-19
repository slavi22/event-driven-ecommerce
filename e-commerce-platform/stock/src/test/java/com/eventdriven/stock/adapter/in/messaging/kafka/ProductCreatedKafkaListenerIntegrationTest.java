package com.eventdriven.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.stock.application.port.in.InitializeStockUseCase;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
import java.util.concurrent.TimeUnit;

import static adapter.Constants.DLT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StockTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProductCreatedKafkaListenerIntegrationTest {

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
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private GetStockCommandPort getStockCommandPort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private InitializeStockUseCase initializeStockUseCase;

    @Value("${kafka.topics.product-created-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when consumed, then stock should be initialized")
    void testListener_whenProductCreatedEventReceived_shouldInitializeStock() {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductCreatedEventPayload payload = buildPayload(productId, 100);

        // Act
        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertTrue(
                       getStockCommandPort.getStockByProductId(productId).isPresent()));
    }

    @Test
    @DisplayName("Given the same ProductCreatedEvent received multiple times, then stock should only be initialized once")
    void testListener_whenDuplicateProductCreatedEvent_shouldBeIdempotent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductCreatedEventPayload payload = buildPayload(productId, 50);

        // Act
        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));
        }

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertTrue(
                       getStockCommandPort.getStockByProductId(productId).isPresent()));
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(1, outboxEventJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given the listener throws an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure"))
                .when(initializeStockUseCase).initializeStock(any());
        UUID productId = UUID.randomUUID();
        ProductCreatedEventPayload payload = buildPayload(productId, 10);

        // Act
        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        // Assert
        try (KafkaConsumer<String, ProductCreatedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                   .untilAsserted(() -> {
                       ConsumerRecords<String, ProductCreatedEventPayload> records =
                               dltConsumer.poll(Duration.ofMillis(500));
                       assertFalse(records.isEmpty());
                       ConsumerRecord<String, ProductCreatedEventPayload> record = records.iterator().next();
                       assertEquals(productId.toString(), record.key());
                   });
        }
    }

    private ProductCreatedEventPayload buildPayload(UUID productId, int quantity) {
        return new ProductCreatedEventPayload(
                productId.toString(), "Test Product", "A test product",
                new BigDecimal("9.99"), ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE, quantity, Instant.now()
        );
    }

    private KafkaConsumer<String, ProductCreatedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ProductCreatedEventPayload.class.getName()
        ));
    }
}
