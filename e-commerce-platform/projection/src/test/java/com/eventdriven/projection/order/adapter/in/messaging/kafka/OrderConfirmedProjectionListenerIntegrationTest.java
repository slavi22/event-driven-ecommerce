package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderConfirmedEventPayload;
import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.projection.config.ProjectionTestConfiguration;
import com.eventdriven.projection.order.adapter.out.persistence.postgres.OrderReadJpaRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProjectionTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OrderConfirmedProjectionListenerIntegrationTest {

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
    private OrderReadJpaRepository orderReadJpaRepository;

    @Value("${kafka.topics.order-placed-topic}")
    private String orderPlacedTopic;
    @Value("${kafka.topics.order-confirmed-topic}")
    private String orderConfirmedTopic;

    @BeforeEach
    void clean() {
        orderReadJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given an OrderConfirmedEvent after an OrderPlacedEvent, when consumed, then the order status should be updated to CONFIRMED")
    void testListener_whenOrderConfirmedEventReceived_shouldUpdateStatusToConfirmed() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        kafkaTemplate.send(orderPlacedTopic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPlacedPayload(orderId)));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, orderReadJpaRepository.findAll().size()));

        kafkaTemplate.send(orderConfirmedTopic, orderId.toString(),
                jsonMapper.writeValueAsString(new OrderConfirmedEventPayload(orderId.toString(), Instant.now())));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals("CONFIRMED", orderReadJpaRepository.findById(orderId).orElseThrow().getStatus()));
    }

    @Test
    @DisplayName("Given an OrderConfirmedEvent for a non-existent order, when consumed, then it should be silently skipped")
    void testListener_whenOrderDoesNotExistInProjection_shouldSkipWithoutError() {
        // Arrange
        UUID nonExistentOrderId = UUID.randomUUID();

        // Act & Assert
        kafkaTemplate.send(orderConfirmedTopic, nonExistentOrderId.toString(),
                jsonMapper.writeValueAsString(
                        new OrderConfirmedEventPayload(nonExistentOrderId.toString(), Instant.now())));

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(0, orderReadJpaRepository.findAll().size()));
    }

    private OrderPlacedEventPayload buildPlacedPayload(UUID orderId) {
        return new OrderPlacedEventPayload(
                orderId.toString(),
                UUID.randomUUID().toString(),
                List.of(new OrderItemPayload(UUID.randomUUID().toString(), 1)),
                new BigDecimal("30.00"),
                Instant.now());
    }
}
