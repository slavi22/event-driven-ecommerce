package com.eventdriven.order.application.service.command;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.order.application.command.OrderItemCommand;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.exception.ProductPriceNotAvailableException;
import com.eventdriven.order.application.mapper.OrderApplicationMapper;
import com.eventdriven.order.application.port.out.command.SaveOrderPort;
import com.eventdriven.order.application.port.out.outbox.OutboxEvent;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.order.application.port.out.productprice.GetCachedProductPricePort;
import com.eventdriven.order.application.port.out.sagastate.SagaState;
import com.eventdriven.order.application.port.out.sagastate.SaveSagaStatePort;
import com.eventdriven.order.domain.entity.Order;
import com.eventdriven.order.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

    @Mock
    private GetCachedProductPricePort getCachedProductPricePort;
    @Mock
    private SaveOrderPort saveOrderPort;
    @Mock
    private SaveSagaStatePort saveSagaStatePort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private OrderApplicationMapper orderApplicationMapper;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private PlaceOrderService placeOrderService;

    @Test
    @DisplayName("Given a valid command with cached prices, when placing an order, then order, saga state and outbox event should be saved")
    void testPlaceOrder_withValidCommand_shouldSaveOrderSagaStateAndOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getCachedProductPricePort.getPrice(productId))
                .thenReturn(Optional.of(Money.of(new BigDecimal("10.00"))));

        // Act
        placeOrderService.placeOrder(new PlaceOrderCommand(UUID.randomUUID(),
                                                           List.of(new OrderItemCommand(productId, 2))));

        // Assert
        verify(saveOrderPort, times(1)).save(any(Order.class));
        verify(saveSagaStatePort, times(1)).save(any(SagaState.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(OrderPlacedEventPayload.class));
    }

    @Test
    @DisplayName("Given a command with a missing product price, when placing an order, then should throw and not save anything")
    void testPlaceOrder_withMissingProductPrice_shouldThrowAndNotSave() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getCachedProductPricePort.getPrice(productId)).thenReturn(Optional.empty());
        PlaceOrderCommand command = new PlaceOrderCommand(UUID.randomUUID(),
                                                          List.of(new OrderItemCommand(productId, 1)));

        // Act & Assert
        assertThrows(ProductPriceNotAvailableException.class, () -> placeOrderService.placeOrder(command));

        verify(saveOrderPort, never()).save(any());
        verify(saveSagaStatePort, never()).save(any());
        verify(saveOutboxEventPort, never()).save(any());
    }
}
