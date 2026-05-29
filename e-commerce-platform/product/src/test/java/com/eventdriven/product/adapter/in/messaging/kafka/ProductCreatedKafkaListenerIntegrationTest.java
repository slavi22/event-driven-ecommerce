package com.eventdriven.product.adapter.in.messaging.kafka;

import com.eventdriven.product.adapter.out.persistence.query.postgres.ProductReadJpaRepository;
import com.eventdriven.product.application.port.out.persistence.query.SaveProductQueryPort;
import com.eventdriven.product.config.ProductTestConfiguration;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductCreatedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProductCreatedKafkaListenerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> commandDb =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");
    @Container
    static PostgreSQLContainer<?> queryDb =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.command.jdbc-url", commandDb::getJdbcUrl);
        registry.add("spring.datasource.command.username", commandDb::getUsername);
        registry.add("spring.datasource.command.password", commandDb::getPassword);
        registry.add("spring.datasource.query.jdbc-url", queryDb::getJdbcUrl);
        registry.add("spring.datasource.query.username", queryDb::getUsername);
        registry.add("spring.datasource.query.password", queryDb::getPassword);
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
        // Arrange
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(),
                "product-name",
                "product-description",
                new BigDecimal("10.00"),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                10,
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(1, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when the same event is consumed multiple times by the Kafka listener, then the projection table should not have duplicate entries for the same product")
    void testProjectionTable_whenDuplicateProductCreatedEvent_shouldNotCreateDuplicateEntryInProjectionTable() {
        // Arrange
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(),
                "product-name",
                "product-description",
                new BigDecimal("10.00"),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                10,
                Instant.now()
        );
        int loopCount = 3;

        // Act
        for (int i = 0; i < loopCount; i++) {
            kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));
        }

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(1, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given multiple distinct ProductCreatedEvents, when they are consumed by the Kafka listener, then each product should be saved as a separate entry in the projection table")
    void testProjectionTable_whenMultipleDistinctProductCreatedEvents_shouldSaveEachProductSeparately() {
        // Arrange
        ProductCreatedEventPayload firstPayload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(),
                "first-product",
                "first-product-description",
                new BigDecimal("10.00"),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                10,
                Instant.now()
        );
        ProductCreatedEventPayload secondPayload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(),
                "second-product",
                "second-product-description",
                new BigDecimal("20.00"),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                10,
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, firstPayload.productId(), jsonMapper.writeValueAsString(firstPayload));
        kafkaTemplate.send(topic, secondPayload.productId(), jsonMapper.writeValueAsString(secondPayload));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(2, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given a ProductCreatedEvent, when the Kafka listener encounters an exception while processing the event, then the message should be sent to the Dead Letter Topic (DLT)")
    void testKafkaDlt_whenListenerEncountersException_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(saveProductQueryPort).save(any(Product.class));
        ProductCreatedEventPayload payload = new ProductCreatedEventPayload(
                UUID.randomUUID().toString(),
                "product-name",
                "product-description",
                new BigDecimal("10.00"),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                10,
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        // Assert
        try (KafkaConsumer<String, ProductCreatedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + ".DLT"));
            await().atMost(10, TimeUnit.SECONDS)
                   .untilAsserted(() -> {
                       ConsumerRecords<String, ProductCreatedEventPayload>
                               consumerRecords = dltConsumer.poll(Duration.ofMillis(500));
                       assertFalse(consumerRecords.isEmpty());

                       ConsumerRecord<String, ProductCreatedEventPayload> consumerRecord =
                               consumerRecords.iterator().next();

                       assertEquals(payload.productId(), consumerRecord.value().productId());
                       assertEquals(payload.name(), consumerRecord.value().name());
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
