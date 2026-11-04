package com.eventdriven.order.application.service.integration;

import com.eventdriven.contracts.order.event.OrderCancelledEventPayload;
import com.eventdriven.contracts.order.event.OrderConfirmedEventPayload;
import com.eventdriven.contracts.order.event.OrderReadyForPaymentEventPayload;
import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
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
import com.eventdriven.order.application.port.out.sagastate.SagaStep;
import com.eventdriven.order.application.service.command.OrderSagaOrchestrator;
import com.eventdriven.order.config.OrderTestConfiguration;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OrderSagaOrchestratorIntegrationTest {

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

    private UUID productId;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepository.deleteAll();
        productId = UUID.randomUUID();
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("15.00")));
    }

    @Test
    @DisplayName("onStockReserved: happy path - order should advance to STOCK_RESERVED and saga to PAYMENT")
    void testOnStockReserved_happyPath_shouldAdvanceSagaToPayment() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));

        // Assert
        assertEquals(OrderStatus.STOCK_RESERVED, getOrderPort.getOrder(placed.orderId()).getStatus());
        var saga = getSagaStatePort.getSagaState(placed.orderId());
        assertEquals(SagaStep.PAYMENT, saga.currentStep());
        assertEquals(SagaStatus.STARTED, saga.status());
        assertEquals(1, outboxEventJpaRepository.findAll().size());
        assertEquals(OrderReadyForPaymentEventPayload.EVENT_TYPE,
                outboxEventJpaRepository.findAll().getFirst().getEventType());
    }

    @Test
    @DisplayName("onStockReserved: duplicate delivery when saga is past STOCK_RESERVATION should be a no-op")
    void testOnStockReserved_duplicate_shouldBeNoOp() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));

        // Assert
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }

    @Test
    @DisplayName("onStockReservationFailed: should cancel order, mark saga FAILED, and save OrderCancelled outbox event")
    void testOnStockReservationFailed_shouldCancelOrderAndFailSaga() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onStockReservationFailed(new StockReservationFailedEventPayload(
                placed.orderId().toString(), productId.toString(), "Out of stock", Instant.now()));

        // Assert
        assertEquals(OrderStatus.CANCELLED, getOrderPort.getOrder(placed.orderId()).getStatus());
        assertEquals(SagaStatus.FAILED, getSagaStatePort.getSagaState(placed.orderId()).status());
        assertEquals(1, outboxEventJpaRepository.findAll().size());
        assertEquals(OrderCancelledEventPayload.EVENT_TYPE,
                outboxEventJpaRepository.findAll().getFirst().getEventType());
    }

    @Test
    @DisplayName("onPaymentProcessed: should confirm order, mark saga COMPLETED, and save OrderConfirmed outbox event")
    void testOnPaymentProcessed_shouldConfirmOrderAndCompleteSaga() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onPaymentProcessed(
                new PaymentProcessedEventPayload(placed.orderId().toString(), BigDecimal.TEN, Instant.now()));

        // Assert
        assertEquals(OrderStatus.CONFIRMED, getOrderPort.getOrder(placed.orderId()).getStatus());
        var saga = getSagaStatePort.getSagaState(placed.orderId());
        assertEquals(SagaStep.COMPLETED, saga.currentStep());
        assertEquals(SagaStatus.COMPLETED, saga.status());
        assertEquals(1, outboxEventJpaRepository.findAll().size());
        assertEquals(OrderConfirmedEventPayload.EVENT_TYPE,
                outboxEventJpaRepository.findAll().getFirst().getEventType());
    }

    @Test
    @DisplayName("onPaymentFailed: should move order to CANCELLING, saga to COMPENSATING, and save OrderCancelled outbox event")
    void testOnPaymentFailed_shouldStartCompensation() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onPaymentFailed(
                new PaymentFailedEventPayload(placed.orderId().toString(), "Card declined", Instant.now()));

        // Assert
        assertEquals(OrderStatus.CANCELLING, getOrderPort.getOrder(placed.orderId()).getStatus());
        var saga = getSagaStatePort.getSagaState(placed.orderId());
        assertEquals(SagaStep.COMPENSATING_STOCK, saga.currentStep());
        assertEquals(SagaStatus.COMPENSATING, saga.status());
        assertEquals(1, outboxEventJpaRepository.findAll().size());
        assertEquals(OrderCancelledEventPayload.EVENT_TYPE,
                outboxEventJpaRepository.findAll().getFirst().getEventType());
    }

    @Test
    @DisplayName("onStockReleased: should cancel order, mark saga FAILED, and save no outbox event")
    void testOnStockReleased_shouldCancelOrderAndFailSaga() {
        // Arrange
        PlaceOrderResult placed = placeOrder();
        sagaOrchestrator.onStockReserved(stockReservedPayload(placed.orderId()));
        sagaOrchestrator.onPaymentFailed(
                new PaymentFailedEventPayload(placed.orderId().toString(), "Card declined", Instant.now()));
        outboxEventJpaRepository.deleteAll();

        // Act
        sagaOrchestrator.onStockReleased(new StockReleasedEventPayload(
                placed.orderId().toString(), productId.toString(), 2, Instant.now()));

        // Assert
        assertEquals(OrderStatus.CANCELLED, getOrderPort.getOrder(placed.orderId()).getStatus());
        assertEquals(SagaStatus.FAILED, getSagaStatePort.getSagaState(placed.orderId()).status());
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }

    private PlaceOrderResult placeOrder() {
        return placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(UUID.randomUUID(), List.of(new OrderItemCommand(productId, 2))));
    }

    private StockReservedEventPayload stockReservedPayload(UUID orderId) {
        return new StockReservedEventPayload(
                UUID.randomUUID().toString(), productId.toString(),
                orderId.toString(), 2, 98, Instant.now());
    }
}
