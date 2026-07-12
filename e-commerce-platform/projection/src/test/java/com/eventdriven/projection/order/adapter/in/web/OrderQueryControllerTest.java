package com.eventdriven.projection.order.adapter.in.web;

import com.eventdriven.projection.order.adapter.in.web.dto.response.GetOrderResponse;
import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.port.in.GetAllOrdersQueryUseCase;
import com.eventdriven.projection.order.application.port.in.GetOrderQueryUseCase;
import com.eventdriven.projection.order.application.query.GetAllOrdersQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderQueryController.class)
class OrderQueryControllerTest {

    @MockitoBean
    private GetOrderQueryUseCase getOrderQueryUseCase;
    @MockitoBean
    private GetAllOrdersQueryUseCase getAllOrdersQueryUseCase;
    @MockitoBean
    private OrderWebMapper orderWebMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Given a valid X-User-Id header, when getting all orders, then should return only that user's orders")
    void testGetAllOrders_withValidUserIdHeader_shouldReturnUsersOrders() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        GetOrderResult result = new GetOrderResult(UUID.randomUUID(), userId,
                new BigDecimal("100.00"), "CONFIRMED", Instant.now(), Instant.now(), List.of());
        GetOrderResponse response = new GetOrderResponse(result.orderId(), userId,
                result.totalAmount(), result.status(), result.createdAt(), result.updatedAt(), List.of());

        when(getAllOrdersQueryUseCase.getAllOrders(eq(new GetAllOrdersQuery(userId))))
                .thenReturn(List.of(result));
        when(orderWebMapper.toGetOrderResponse(result)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(result.orderId().toString()))
                .andExpect(jsonPath("$[0].customerId").value(userId.toString()));

        verify(getAllOrdersQueryUseCase).getAllOrders(new GetAllOrdersQuery(userId));
    }

    @Test
    @DisplayName("Given no X-User-Id header, when getting all orders, then should fail")
    void testGetAllOrders_withoutUserIdHeader_shouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Given no orders for the user, when getting all orders, then should return empty list")
    void testGetAllOrders_whenUserHasNoOrders_shouldReturnEmptyList() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(getAllOrdersQueryUseCase.getAllOrders(any(GetAllOrdersQuery.class))).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
