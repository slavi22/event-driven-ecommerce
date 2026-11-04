package com.eventdriven.order.adapter.in.web;

import com.eventdriven.order.adapter.in.web.dto.request.OrderItemRequest;
import com.eventdriven.order.adapter.in.web.dto.request.PlaceOrderRequest;
import com.eventdriven.order.adapter.in.web.dto.response.PlaceOrderResponse;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.application.exception.ProductPriceNotAvailableException;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderCommandController.class)
class OrderCommandControllerTest {

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;
    @MockitoBean
    private OrderWebMapper orderWebMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("Given a valid request with X-User-Id header, when placing an order, then should return 201 with order ID")
    void testPlaceOrder_withValidRequest_shouldReturn201() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PlaceOrderRequest request = new PlaceOrderRequest(
                List.of(new OrderItemRequest(UUID.randomUUID(), 2)));

        when(orderWebMapper.toPlaceOrderCommand(any(), any(PlaceOrderRequest.class)))
                .thenReturn(new PlaceOrderCommand(userId, List.of()));
        when(placeOrderUseCase.placeOrder(any(PlaceOrderCommand.class)))
                .thenReturn(new PlaceOrderResult(orderId));
        when(orderWebMapper.toPlaceOrderResponse(any(PlaceOrderResult.class)))
                .thenReturn(new PlaceOrderResponse(orderId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .content(jsonMapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("Given a request with an empty items list, when placing an order, then should return 400")
    void testPlaceOrder_withEmptyItems_shouldReturn400() throws Exception {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .content(jsonMapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid request body"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Given a request with null items, when placing an order, then should return 400")
    void testPlaceOrder_withNullItems_shouldReturn400() throws Exception {
        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .content(jsonMapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("Given a valid request but product price is not cached, when placing an order, then should return 422")
    void testPlaceOrder_whenProductPriceNotAvailable_shouldReturn422() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        PlaceOrderRequest request = new PlaceOrderRequest(
                List.of(new OrderItemRequest(UUID.randomUUID(), 1)));

        // Act & Assert
        when(orderWebMapper.toPlaceOrderCommand(any(), any(PlaceOrderRequest.class)))
                .thenReturn(new PlaceOrderCommand(userId, List.of()));
        when(placeOrderUseCase.placeOrder(any(PlaceOrderCommand.class)))
                .thenThrow(new ProductPriceNotAvailableException("No cached price for product"));

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .content(jsonMapper.writeValueAsBytes(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_CONTENT.value()));
    }
}
