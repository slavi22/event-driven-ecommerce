package com.eventdriven.projection.order.application.service.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.dto.OrderItemResult;
import com.eventdriven.projection.order.application.port.out.query.GetAllOrdersQueryPort;
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
    @DisplayName("Getting all orders when orders exist should return list of order results")
    void testGetAllOrders_whenOrdersExist_shouldReturnOrderResultList() {
        // Arrange
        List<GetOrderResult> expected = List.of(
                new GetOrderResult(UUID.randomUUID(), UUID.randomUUID(),
                                   new BigDecimal("100.00"), "CONFIRMED", Instant.now(), Instant.now(),
                                   List.of(new OrderItemResult(UUID.randomUUID(), "Product A", 1))),
                new GetOrderResult(UUID.randomUUID(), UUID.randomUUID(),
                                   new BigDecimal("50.00"), "PENDING", Instant.now(), Instant.now(),
                                   List.of(new OrderItemResult(UUID.randomUUID(), "Product B", 2))));

        when(getAllOrdersQueryPort.getAllOrders()).thenReturn(expected);

        // Act
        List<GetOrderResult> result = getAllOrdersQueryService.getAllOrders();

        // Assert
        assertEquals(expected, result);
        verify(getAllOrdersQueryPort).getAllOrders();
    }

    @Test
    @DisplayName("Getting all orders when no orders exist should return an empty list")
    void testGetAllOrders_whenNoOrdersExist_shouldReturnEmptyList() {
        // Arrange
        when(getAllOrdersQueryPort.getAllOrders()).thenReturn(List.of());

        // Act
        List<GetOrderResult> result = getAllOrdersQueryService.getAllOrders();

        // Assert
        assertTrue(result.isEmpty());
        verify(getAllOrdersQueryPort).getAllOrders();
    }
}
