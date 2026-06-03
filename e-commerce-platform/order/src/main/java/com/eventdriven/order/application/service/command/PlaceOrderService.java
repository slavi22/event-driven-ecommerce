package com.eventdriven.order.application.service.command;

import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.application.exception.ProductPriceNotAvailableException;
import com.eventdriven.order.application.mapper.OrderApplicationMapper;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import com.eventdriven.order.application.port.out.command.SaveOrderPort;
import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.order.application.port.out.productprice.GetCachedProductPricePort;
import com.eventdriven.order.application.port.out.sagastate.SagaState;
import com.eventdriven.order.application.port.out.sagastate.SagaStatus;
import com.eventdriven.order.application.port.out.sagastate.SagaStep;
import com.eventdriven.order.application.port.out.sagastate.SaveSagaStatePort;
import com.eventdriven.order.domain.entity.Order;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
class PlaceOrderService implements PlaceOrderUseCase {

    private final GetCachedProductPricePort getCachedProductPricePort;
    private final SaveOrderPort saveOrderPort;
    private final SaveSagaStatePort saveSagaStatePort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final OrderApplicationMapper orderApplicationMapper;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        List<OrderItem> items = command.items()
                                       .stream()
                                       .map(orderItemCommand -> {
                                           Money unitPrice =
                                                   getCachedProductPricePort.getPrice(orderItemCommand.productId())
                                                                            .orElseThrow(
                                                                                    () -> new ProductPriceNotAvailableException(
                                                                                            "No cached price for product " +
                                                                                            orderItemCommand.productId()));
                                           return OrderItem.of(orderItemCommand.productId(),
                                                               orderItemCommand.quantity(), unitPrice);
                                       })
                                       .toList();
        Money totalAmount = items.stream().map(OrderItem::lineTotal).reduce(Money.of(BigDecimal.ZERO), Money::add);
        Order order = Order.place(command.customerId(), items, totalAmount);
        saveOrderPort.save(order);
        saveSagaStatePort.save(
                new SagaState(order.getId().getValue(), SagaStep.STOCK_RESERVATION, SagaStatus.STARTED, Instant.now(),
                              Instant.now()));

        saveOutboxEventPort.save(new OutboxEvent(
                order.getId().getValue().toString(),
                OrderPlacedEventPayload.AGGREGATE_TYPE,
                OrderPlacedEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new OrderPlacedEventPayload(
                        order.getId().getValue().toString(),
                        command.customerId().toString(),
                        items.stream().map(orderItem -> new OrderItemPayload(orderItem.getProductId().toString(),
                                                                             orderItem.getQuantity())).toList(),
                        totalAmount.getAmount(),
                        Instant.now())),
                Instant.now()));

        log.info("Order {} placed, saga started (STOCK_RESERVATION)", order.getId().getValue());
        return orderApplicationMapper.toPlaceOrderResult(order);
    }
}
