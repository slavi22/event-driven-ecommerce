package com.eventdriven.projection.product.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.projection.product.adapter.out.persistence.postgres.ProductReadJpaRepository;
import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.port.out.persistence.SaveProductQueryPort;
import com.eventdriven.projection.config.ProjectionTestConfiguration;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProjectionTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProductCreatedKafkaListenerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("projection");

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
    private ProductReadJpaRepository productReadJpaRepository;
    @Value("${kafka.topics.product-created-topic}")
    private String topic;

    @MockitoSpyBean
    private SaveProductQueryPort saveProductQueryPort;

    @BeforeEach
    void clean() {
        productReadJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when the event is consumed by the Kafka listener, then the projection table should be populated with the new product data")
    void testProjectionTableForEventualConsistency_whenReceiveProductCreatedEvent_shouldPopulateProjectionTable() {
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(), "product-name", "product-description",
                new BigDecimal("10.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                10, Instant.now());

        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(1, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when the same event is consumed multiple times, then the projection table should not have duplicate entries")
    void testProjectionTable_whenDuplicateProductCreatedEvent_shouldNotCreateDuplicateEntryInProjectionTable() {
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(), "product-name", "product-description",
                new BigDecimal("10.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                10, Instant.now());

        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));
        }

        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(1, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given multiple distinct ProductCreatedEvents, when they are consumed, then each product should be saved as a separate entry")
    void testProjectionTable_whenMultipleDistinctProductCreatedEvents_shouldSaveEachProductSeparately() {
        ProductCreatedEventPayload first = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(), "first-product", "first-description",
                new BigDecimal("10.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                10, Instant.now());
        ProductCreatedEventPayload second = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(), "second-product", "second-description",
                new BigDecimal("20.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                10, Instant.now());

        kafkaTemplate.send(topic, first.productId(), jsonMapper.writeValueAsString(first));
        kafkaTemplate.send(topic, second.productId(), jsonMapper.writeValueAsString(second));

        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(2, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when the listener encounters an exception, then the message should be sent to the DLT")
    void testKafkaDlt_whenListenerEncountersException_shouldSendMessageToDlt() {
        doThrow(new RuntimeException("Simulated failure")).when(saveProductQueryPort).save(any(GetProductResult.class));
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(), "product-name", "product-description",
                new BigDecimal("10.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                10, Instant.now());

        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        try (KafkaConsumer<String, ProductCreatedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(10, TimeUnit.SECONDS)
                   .untilAsserted(() -> {
                       ConsumerRecords<String, ProductCreatedEventPayload> records =
                               dltConsumer.poll(Duration.ofMillis(500));
                       assertFalse(records.isEmpty());
                       ConsumerRecord<String, ProductCreatedEventPayload> record = records.iterator().next();
                       assertEquals(payload.productId(), record.value().productId());
                       assertEquals(payload.name(), record.value().name());
                   });
        }
    }

    private KafkaConsumer<String, ProductCreatedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ProductCreatedEventPayload.class.getName()
        ));
    }
}
