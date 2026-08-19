package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;
import com.eventdriven.contracts.product.event.ProductUpdatedEventPayload;
import com.eventdriven.order.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.order.application.port.out.productprice.GetCachedProductPricePort;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.config.OrderTestConfiguration;
import com.eventdriven.order.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProductUpdatedKafkaListenerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("order");

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
    private GetCachedProductPricePort getCachedProductPricePort;
    @Autowired
    private UpsertProductPricePort upsertProductPricePort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Value("${kafka.topics.product-updated-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a ProductUpdatedEvent, when consumed, then the cached price should be updated")
    void testListener_whenProductUpdatedEventReceived_shouldUpdateCachedPrice() {
        // Arrange
        UUID productId = UUID.randomUUID();
        ProductUpdatedEventPayload payload = new ProductUpdatedEventPayload(
                productId.toString(), "Updated Product", "Updated description",
                new BigDecimal("25.00"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                Instant.now(), Instant.now());

        // Act & Assert
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("10.00")));

        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var price = getCachedProductPricePort.getPrice(productId);
                    assertTrue(price.isPresent());
                    assertEquals(0, new BigDecimal("25.00").compareTo(price.get().getAmount()));
                });
    }

    @Test
    @DisplayName("Given a ProductUpdatedEvent for a non-cached product, when consumed, then the price should be cached")
    void testListener_whenProductNotPreviouslyCached_shouldUpsertPrice() {
        // Arrange
        UUID productId = UUID.randomUUID();

        ProductUpdatedEventPayload payload = new ProductUpdatedEventPayload(
                productId.toString(), "New Product", "Description",
                new BigDecimal("49.99"), ProductCategory.ELECTRONICS, ProductStatus.ACTIVE,
                Instant.now(), Instant.now());

        // Act & Assert
        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(
                        getCachedProductPricePort.getPrice(productId).isPresent()));
    }
}
