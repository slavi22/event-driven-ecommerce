package com.eventdriven.order.application.service.command;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.command.UpdateOrderPort;
import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.order.application.port.out.sagastate.*;
import com.eventdriven.order.domain.entity.Order;
import com.eventdriven.order.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaOrchestratorTest {

    @Mock
    private GetSagaStatePort getSagaStatePort;
    @Mock
    private UpdateSagaStatePort updateSagaStatePort;
    @Mock
    private GetOrderPort getOrderPort;
    @Mock
    private UpdateOrderPort updateOrderPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private OrderSagaOrchestrator sagaOrchestrator;

    @Test
    @DisplayName("onStockReserved: when saga is in STOCK_RESERVATION step, should advance order to STOCK_RESERVED and saga to PAYMENT")
    void testOnStockReserved_happyPath_shouldAdvanceSagaAndSaveOutbox() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.STOCK_RESERVATION, SagaStatus.STARTED));
        when(getOrderPort.getOrder(orderId))
                .thenReturn(buildOrder(orderId, OrderStatus.PENDING));

        // Act
        sagaOrchestrator.onStockReserved(stockReservedPayload(orderId));

        // Assert
        verify(updateOrderPort).update(any());
        verify(updateSagaStatePort).update(orderId, SagaStep.PAYMENT, SagaStatus.STARTED);
        verify(saveOutboxEventPort).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("onStockReserved: when saga is not in STOCK_RESERVATION step (duplicate), should be a no-op")
    void testOnStockReserved_duplicate_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.PAYMENT, SagaStatus.STARTED));

        // Act
        sagaOrchestrator.onStockReserved(stockReservedPayload(orderId));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(updateSagaStatePort, never()).update(any(), any(), any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onStockReservationFailed: when saga is STARTED, should cancel order and mark saga FAILED")
    void testOnStockReservationFailed_happyPath_shouldCancelOrderAndFailSaga() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.STOCK_RESERVATION, SagaStatus.STARTED));
        when(getOrderPort.getOrder(orderId))
                .thenReturn(buildOrder(orderId, OrderStatus.PENDING));

        // Act
        sagaOrchestrator.onStockReservationFailed(stockReservationFailedPayload(orderId));

        // Assert
        verify(updateOrderPort).update(any());
        verify(updateSagaStatePort).update(orderId, SagaStep.STOCK_RESERVATION, SagaStatus.FAILED);
        verify(saveOutboxEventPort).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("onStockReservationFailed: when saga is already FAILED (duplicate), should be a no-op")
    void testOnStockReservationFailed_whenAlreadyFailed_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.STOCK_RESERVATION, SagaStatus.FAILED));

        // Act
        sagaOrchestrator.onStockReservationFailed(stockReservationFailedPayload(orderId));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onStockReservationFailed: when saga is already COMPLETED (duplicate), should be a no-op")
    void testOnStockReservationFailed_whenAlreadyCompleted_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.COMPLETED, SagaStatus.COMPLETED));

        // Act
        sagaOrchestrator.onStockReservationFailed(stockReservationFailedPayload(orderId));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onPaymentProcessed: when saga is in PAYMENT step, should confirm order and complete saga")
    void testOnPaymentProcessed_happyPath_shouldConfirmOrderAndCompleteSaga() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.PAYMENT, SagaStatus.STARTED));
        when(getOrderPort.getOrder(orderId))
                .thenReturn(buildOrder(orderId, OrderStatus.STOCK_RESERVED));

        // Act
        sagaOrchestrator.onPaymentProcessed(
                new PaymentProcessedEventPayload(orderId.toString(), BigDecimal.TEN, Instant.now()));

        // Assert
        verify(updateOrderPort).update(any());
        verify(updateSagaStatePort).update(orderId, SagaStep.COMPLETED, SagaStatus.COMPLETED);
        verify(saveOutboxEventPort).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("onPaymentProcessed: when saga is not in PAYMENT step (duplicate), should be a no-op")
    void testOnPaymentProcessed_duplicate_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.COMPLETED, SagaStatus.COMPLETED));

        // Act
        sagaOrchestrator.onPaymentProcessed(
                new PaymentProcessedEventPayload(orderId.toString(), BigDecimal.TEN, Instant.now()));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onPaymentFailed: when saga is in PAYMENT step, should start cancellation and begin stock compensation")
    void testOnPaymentFailed_happyPath_shouldStartCancellationAndBeginCompensation() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.PAYMENT, SagaStatus.STARTED));
        when(getOrderPort.getOrder(orderId))
                .thenReturn(buildOrder(orderId, OrderStatus.STOCK_RESERVED));

        // Act
        sagaOrchestrator.onPaymentFailed(
                new PaymentFailedEventPayload(orderId.toString(), "insufficient funds", Instant.now()));

        // Assert
        verify(updateOrderPort).update(any());
        verify(updateSagaStatePort).update(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.COMPENSATING);
        verify(saveOutboxEventPort).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("onPaymentFailed: when saga is not in PAYMENT step (duplicate), should be a no-op")
    void testOnPaymentFailed_duplicate_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.COMPENSATING));

        // Act
        sagaOrchestrator.onPaymentFailed(
                new PaymentFailedEventPayload(orderId.toString(), "reason", Instant.now()));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onStockReleased: when saga is in COMPENSATING_STOCK step, should cancel order and fail saga")
    void testOnStockReleased_happyPath_shouldCancelOrderAndFailSaga() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.COMPENSATING));
        when(getOrderPort.getOrder(orderId))
                .thenReturn(buildOrder(orderId, OrderStatus.CANCELLING));

        // Act
        sagaOrchestrator.onStockReleased(
                new StockReleasedEventPayload(orderId.toString(), UUID.randomUUID().toString(), 5, Instant.now()));

        // Assert
        verify(updateOrderPort).update(any());
        verify(updateSagaStatePort).update(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.FAILED);
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("onStockReleased: when saga is not in COMPENSATING_STOCK step (duplicate), should be a no-op")
    void testOnStockReleased_duplicate_shouldBeNoOp() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getSagaStatePort.getSagaState(orderId))
                .thenReturn(sagaState(orderId, SagaStep.COMPLETED, SagaStatus.FAILED));

        // Act
        sagaOrchestrator.onStockReleased(
                new StockReleasedEventPayload(orderId.toString(), UUID.randomUUID().toString(), 5, Instant.now()));

        // Assert
        verify(updateOrderPort, never()).update(any());
        verify(updateSagaStatePort, never()).update(any(), any(), any());
    }

    private SagaState sagaState(UUID orderId, SagaStep step, SagaStatus status) {
        return new SagaState(orderId, step, status, Instant.now(), Instant.now());
    }

    private Order buildOrder(UUID orderId, OrderStatus status) {
        return Order.reconstitute(
                new OrderId(orderId),
                UUID.randomUUID(),
                List.of(OrderItem.of(UUID.randomUUID(), 1, Money.of(new BigDecimal("10.00")))),
                Money.of(new BigDecimal("10.00")),
                status,
                Instant.now(),
                Instant.now()
        );
    }

    private StockReservedEventPayload stockReservedPayload(UUID orderId) {
        return new StockReservedEventPayload(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                orderId.toString(), 5, 95, Instant.now());
    }

    private StockReservationFailedEventPayload stockReservationFailedPayload(UUID orderId) {
        return new StockReservationFailedEventPayload(
                orderId.toString(), UUID.randomUUID().toString(),
                "Insufficient stock", Instant.now());
    }
}
