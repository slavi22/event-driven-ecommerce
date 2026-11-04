package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.order.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.order.application.command.OrderItemCommand;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.application.port.out.sagastate.GetSagaStatePort;
import com.eventdriven.order.application.port.out.sagastate.SagaStatus;
import com.eventdriven.order.application.service.command.OrderSagaOrchestrator;
import com.eventdriven.order.config.OrderTestConfiguration;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderStatus;
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
        classes = OrderTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class StockReleasedKafkaListenerIntegrationTest {

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
    private PlaceOrderUseCase placeOrderUseCase;
    @Autowired
    private OrderSagaOrchestrator sagaOrchestrator;
    @Autowired
    private GetOrderPort getOrderPort;
    @Autowired
    private GetSagaStatePort getSagaStatePort;
    @Autowired
    private UpsertProductPricePort upsertProductPricePort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Value("${kafka.topics.stock-released-topic}")
    private String topic;

    private UUID productId;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepository.deleteAll();
        productId = UUID.randomUUID();
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Given a StockReleasedEvent after payment failure, when consumed, then order should be CANCELLED and saga FAILED")
    void testListener_whenStockReleasedEventReceived_shouldCancelOrderAndFailSaga() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        sagaOrchestrator.onStockReserved(new StockReservedEventPayload(
                UUID.randomUUID().toString(), productId.toString(),
                placed.orderId().toString(), 1, 99, Instant.now()));
        sagaOrchestrator.onPaymentFailed(new PaymentFailedEventPayload(
                placed.orderId().toString(), "Card declined", Instant.now()));
        StockReleasedEventPayload payload = new StockReleasedEventPayload(
                placed.orderId().toString(), productId.toString(), 1, Instant.now());

        // Act & Assert
        kafkaTemplate.send(topic, placed.orderId().toString(), jsonMapper.writeValueAsString(payload));

        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() ->
                                      assertEquals(OrderStatus.CANCELLED,
                                                   getOrderPort.getOrder(placed.orderId()).getStatus()));

        assertEquals(SagaStatus.FAILED, getSagaStatePort.getSagaState(placed.orderId()).status());
    }

    private PlaceOrderResult placeOrder() {
        return placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(UUID.randomUUID(), List.of(new OrderItemCommand(productId, 1))));
    }
}
