package com.eventdriven.order.application.service.command;

import com.eventdriven.contracts.order.event.OrderCancelledEventPayload;
import com.eventdriven.contracts.order.event.OrderConfirmedEventPayload;
import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.contracts.order.event.OrderReadyForPaymentEventPayload;
import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.command.UpdateOrderPort;
import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.order.application.port.out.sagastate.GetSagaStatePort;
import com.eventdriven.order.application.port.out.sagastate.SagaState;
import com.eventdriven.order.application.port.out.sagastate.SagaStatus;
import com.eventdriven.order.application.port.out.sagastate.SagaStep;
import com.eventdriven.order.application.port.out.sagastate.UpdateSagaStatePort;
import com.eventdriven.order.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final GetSagaStatePort getSagaStatePort;
    private final UpdateSagaStatePort updateSagaStatePort;
    private final GetOrderPort getOrderPort;
    private final UpdateOrderPort updateOrderPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Transactional
    public void onStockReserved(StockReservedEventPayload event) {
        UUID orderId = UUID.fromString(event.orderId());
        SagaState saga = getSagaStatePort.getSagaState(orderId);
        if (saga.currentStep() != SagaStep.STOCK_RESERVATION) {
            log.warn("onStockReserved: duplicate delivery ignored for order {} (saga step: {})",
                    orderId, saga.currentStep());
            return;
        }

        Order order = getOrderPort.getOrder(orderId);
        order.markStockReserved();
        updateOrderPort.update(order);
        updateSagaStatePort.update(orderId, SagaStep.PAYMENT, SagaStatus.STARTED);

        saveOutboxEventPort.save(new OutboxEvent(
                orderId.toString(),
                OrderReadyForPaymentEventPayload.AGGREGATE_TYPE,
                OrderReadyForPaymentEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new OrderReadyForPaymentEventPayload(
                        orderId.toString(),
                        order.getTotalAmount().getAmount(),
                        Instant.now())),
                Instant.now()));

        log.info("onStockReserved: order {} → STOCK_RESERVED, saga → PAYMENT", orderId);
    }

    @Transactional
    public void onStockReservationFailed(StockReservationFailedEventPayload event) {
        UUID orderId = UUID.fromString(event.orderId());
        SagaState saga = getSagaStatePort.getSagaState(orderId);
        if (saga.status() == SagaStatus.FAILED || saga.status() == SagaStatus.COMPLETED) {
            log.warn("onStockReservationFailed: duplicate delivery ignored for order {} (saga status: {})",
                    orderId, saga.status());
            return;
        }

        Order order = getOrderPort.getOrder(orderId);
        order.cancel();
        updateOrderPort.update(order);
        updateSagaStatePort.update(orderId, SagaStep.STOCK_RESERVATION, SagaStatus.FAILED);

        saveOutboxEventPort.save(new OutboxEvent(
                orderId.toString(),
                OrderCancelledEventPayload.AGGREGATE_TYPE,
                OrderCancelledEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new OrderCancelledEventPayload(
                        orderId.toString(),
                        toItemPayloads(order),
                        false,
                        event.reason(),
                        Instant.now())),
                Instant.now()));

        log.info("onStockReservationFailed: order {} → CANCELLED, saga → FAILED (no compensation needed)", orderId);
    }

    @Transactional
    public void onPaymentProcessed(PaymentProcessedEventPayload event) {
        UUID orderId = UUID.fromString(event.orderId());
        SagaState saga = getSagaStatePort.getSagaState(orderId);
        if (saga.currentStep() != SagaStep.PAYMENT) {
            log.warn("onPaymentProcessed: duplicate delivery ignored for order {} (saga step: {})",
                    orderId, saga.currentStep());
            return;
        }

        Order order = getOrderPort.getOrder(orderId);
        order.confirm();
        updateOrderPort.update(order);
        updateSagaStatePort.update(orderId, SagaStep.COMPLETED, SagaStatus.COMPLETED);

        saveOutboxEventPort.save(new OutboxEvent(
                orderId.toString(),
                OrderConfirmedEventPayload.AGGREGATE_TYPE,
                OrderConfirmedEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new OrderConfirmedEventPayload(
                        orderId.toString(),
                        Instant.now())),
                Instant.now()));

        log.info("onPaymentProcessed: order {} → CONFIRMED, saga → COMPLETED", orderId);
    }

    @Transactional
    public void onPaymentFailed(PaymentFailedEventPayload event) {
        UUID orderId = UUID.fromString(event.orderId());
        SagaState saga = getSagaStatePort.getSagaState(orderId);
        if (saga.currentStep() != SagaStep.PAYMENT) {
            log.warn("onPaymentFailed: duplicate delivery ignored for order {} (saga step: {})",
                    orderId, saga.currentStep());
            return;
        }

        Order order = getOrderPort.getOrder(orderId);
        order.startCancellation();
        updateOrderPort.update(order);
        updateSagaStatePort.update(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.COMPENSATING);

        saveOutboxEventPort.save(new OutboxEvent(
                orderId.toString(),
                OrderCancelledEventPayload.AGGREGATE_TYPE,
                OrderCancelledEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new OrderCancelledEventPayload(
                        orderId.toString(),
                        toItemPayloads(order),
                        true,
                        event.reason(),
                        Instant.now())),
                Instant.now()));

        log.info("onPaymentFailed: order {} → CANCELLING, saga → COMPENSATING_STOCK", orderId);
    }

    @Transactional
    public void onStockReleased(StockReleasedEventPayload event) {
        UUID orderId = UUID.fromString(event.orderId());
        SagaState saga = getSagaStatePort.getSagaState(orderId);
        if (saga.currentStep() != SagaStep.COMPENSATING_STOCK) {
            log.warn("onStockReleased: duplicate delivery ignored for order {} (saga step: {})",
                    orderId, saga.currentStep());
            return;
        }

        Order order = getOrderPort.getOrder(orderId);
        order.cancel();
        updateOrderPort.update(order);
        updateSagaStatePort.update(orderId, SagaStep.COMPENSATING_STOCK, SagaStatus.FAILED);

        log.info("onStockReleased: order {} → CANCELLED, saga → FAILED (compensation complete)", orderId);
    }

    private List<OrderItemPayload> toItemPayloads(Order order) {
        return order.getItems().stream()
                .map(i -> new OrderItemPayload(i.getProductId().toString(), i.getQuantity()))
                .toList();
    }
}
