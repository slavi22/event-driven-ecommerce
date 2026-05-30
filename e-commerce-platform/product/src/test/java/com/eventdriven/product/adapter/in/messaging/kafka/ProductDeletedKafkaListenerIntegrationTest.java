package com.eventdriven.product.adapter.in.messaging.kafka;

import com.eventdriven.product.adapter.out.persistence.query.postgres.ProductReadJpaRepository;
import com.eventdriven.product.application.port.out.persistence.query.DeleteProductQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.SaveProductQueryPort;
import com.eventdriven.product.config.ProductTestConfiguration;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.contracts.product.event.ProductDeletedEventPayload;
import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
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

import static adapter.Constants.DLT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProductDeletedKafkaListenerIntegrationTest {

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
    @Autowired
    private SaveProductQueryPort saveProductQueryPort;
    @Value("${kafka.topics.product-deleted-topic}")
    private String topic;

    @MockitoSpyBean
    private DeleteProductQueryPort deleteProductQueryPort;

    @BeforeEach
    void clean() {
        productReadJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a product exists in the projection, when a ProductDeletedEvent is received, then the projection should be marked as inactive")
    void testProjectionTable_whenReceiveProductDeletedEvent_shouldMarkProductAsInactiveInProjection() {
        // Arrange
        Product existingProduct = buildProduct();
        saveProductQueryPort.save(existingProduct);

        ProductDeletedEventPayload payload = new ProductDeletedEventPayload(
                existingProduct.getId().getValue().toString(),
                existingProduct.getName(),
                existingProduct.getDescription(),
                existingProduct.getPrice().getAmount(),
                existingProduct.getCategory(),
                ProductStatus.INACTIVE,
                existingProduct.getCreatedAt(),
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   var entity = productReadJpaRepository.findById(UUID.fromString(payload.productId())).orElseThrow();
                   assertEquals(ProductStatus.INACTIVE, entity.getStatus());
               });
    }

    @Test
    @DisplayName("Given a product does not exist in the projection, when a ProductDeletedEvent is received, then the event should be skipped")
    void testProjectionTable_whenProductNotFoundInProjection_shouldSkipEvent() {
        // Arrange
        String randomProductId = UUID.randomUUID().toString();
        ProductDeletedEventPayload payload = new ProductDeletedEventPayload(
                randomProductId,
                "Some Product",
                "Some description",
                new BigDecimal("9.99"),
                ProductCategory.ELECTRONICS,
                ProductStatus.INACTIVE,
                Instant.now(),
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        // Assert
        await().pollDelay(3, TimeUnit.SECONDS)
               .atMost(5, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(0, productReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given a product exists in the projection, when the listener encounters an exception, then the message should be sent to the DLT")
    void testKafkaDlt_whenListenerEncountersException_shouldSendMessageToDlt() {
        // Arrange
        Product existingProduct = buildProduct();
        saveProductQueryPort.save(existingProduct);
        doThrow(new RuntimeException("Simulated failure")).when(deleteProductQueryPort).deleteProductById(any(ProductId.class));

        ProductDeletedEventPayload payload = new ProductDeletedEventPayload(
                existingProduct.getId().getValue().toString(),
                existingProduct.getName(),
                existingProduct.getDescription(),
                existingProduct.getPrice().getAmount(),
                existingProduct.getCategory(),
                ProductStatus.INACTIVE,
                existingProduct.getCreatedAt(),
                Instant.now()
        );

        // Act
        kafkaTemplate.send(topic, payload.productId(), jsonMapper.writeValueAsString(payload));

        // Assert
        try (KafkaConsumer<String, ProductDeletedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(10, TimeUnit.SECONDS)
                   .untilAsserted(() -> {
                       ConsumerRecords<String, ProductDeletedEventPayload>
                               records = dltConsumer.poll(Duration.ofMillis(500));
                       assertFalse(records.isEmpty());

                       ConsumerRecord<String, ProductDeletedEventPayload> consumerRecord = records.iterator().next();
                       assertEquals(payload.productId(), consumerRecord.value().productId());
                       assertEquals(payload.name(), consumerRecord.value().name());
                   });
        }
    }

    private Product buildProduct() {
        return Product.reconstitute(
                new ProductId(UUID.randomUUID()),
                "Original Name", "Original Description",
                Money.of(new BigDecimal("10.00")),
                ProductCategory.ELECTRONICS,
                ProductStatus.ACTIVE,
                Instant.now(), null);
    }

    private KafkaConsumer<String, ProductDeletedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ProductDeletedEventPayload.class.getName()
        ));
    }
}
