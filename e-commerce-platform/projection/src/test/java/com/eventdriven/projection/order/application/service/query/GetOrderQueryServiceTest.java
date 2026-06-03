package com.eventdriven.projection.order.application.service.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.exception.OrderNotFoundException;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.query.GetOrderQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderQueryServiceTest {

    @Mock
    private GetOrderQueryPort getOrderQueryPort;

    @InjectMocks
    private GetOrderQueryService getOrderQueryService;

    @Test
    @DisplayName("Getting an order with an existing ID should return the order result")
    void testGetOrder_withExistingOrder_shouldReturnGetOrderResult() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        GetOrderResult expected = new GetOrderResult(
                orderId, UUID.randomUUID(), new BigDecimal("50.00"),
                "PENDING", Instant.now(), Instant.now());

        when(getOrderQueryPort.getOrderById(orderId)).thenReturn(Optional.of(expected));

        // Act
        GetOrderResult result = getOrderQueryService.getOrder(new GetOrderQuery(orderId));

        // Assert
        assertEquals(expected, result);
        verify(getOrderQueryPort).getOrderById(orderId);
    }

    @Test
    @DisplayName("Getting an order with a non-existing ID should throw OrderNotFoundException")
    void testGetOrder_withNonExistingId_shouldThrowOrderNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getOrderQueryPort.getOrderById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class,
                () -> getOrderQueryService.getOrder(new GetOrderQuery(orderId)));
        verify(getOrderQueryPort).getOrderById(orderId);
    }
}
