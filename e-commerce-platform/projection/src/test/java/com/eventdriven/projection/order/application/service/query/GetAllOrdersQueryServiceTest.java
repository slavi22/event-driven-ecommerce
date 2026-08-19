package com.eventdriven.projection.order.application.service.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.dto.OrderItemResult;
import com.eventdriven.projection.order.application.port.out.query.GetAllOrdersQueryPort;
import com.eventdriven.projection.order.application.query.GetAllOrdersQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllOrdersQueryServiceTest {

    @Mock
    private GetAllOrdersQueryPort getAllOrdersQueryPort;

    @InjectMocks
    private GetAllOrdersQueryService getAllOrdersQueryService;

    @Test
    @DisplayName("Getting all orders when orders exist for the customer should return list of order results")
    void testGetAllOrders_whenOrdersExist_shouldReturnOrderResultList() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        List<GetOrderResult> expected = List.of(
                new GetOrderResult(UUID.randomUUID(), customerId,
                                   new BigDecimal("100.00"), "CONFIRMED", Instant.now(), Instant.now(),
                                   List.of(new OrderItemResult(UUID.randomUUID(), "Product A", 1))),
                new GetOrderResult(UUID.randomUUID(), customerId,
                                   new BigDecimal("50.00"), "PENDING", Instant.now(), Instant.now(),
                                   List.of(new OrderItemResult(UUID.randomUUID(), "Product B", 2))));

        when(getAllOrdersQueryPort.getAllOrders(customerId)).thenReturn(expected);

        // Act
        List<GetOrderResult> result = getAllOrdersQueryService.getAllOrders(new GetAllOrdersQuery(customerId));

        // Assert
        assertEquals(expected, result);
        verify(getAllOrdersQueryPort).getAllOrders(customerId);
    }

    @Test
    @DisplayName("Getting all orders when the customer has no orders should return an empty list")
    void testGetAllOrders_whenNoOrdersExist_shouldReturnEmptyList() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        when(getAllOrdersQueryPort.getAllOrders(customerId)).thenReturn(List.of());

        // Act
        List<GetOrderResult> result = getAllOrdersQueryService.getAllOrders(new GetAllOrdersQuery(customerId));

        // Assert
        assertTrue(result.isEmpty());
        verify(getAllOrdersQueryPort).getAllOrders(customerId);
    }
}
