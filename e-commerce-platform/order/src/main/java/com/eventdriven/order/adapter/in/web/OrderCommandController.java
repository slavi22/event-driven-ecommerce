package com.eventdriven.order.adapter.in.web;

import com.eventdriven.order.adapter.in.web.swagger.PlaceOrderOperation;
import com.eventdriven.order.adapter.in.web.dto.request.PlaceOrderRequest;
import com.eventdriven.order.adapter.in.web.dto.response.PlaceOrderResponse;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order Commands", description = "Endpoints for placing orders")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
class OrderCommandController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderWebMapper orderWebMapper;

    @PlaceOrderOperation
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            @Parameter(description = "ID of the authenticated user, injected by the API gateway", example = "user-123", required = true)
            @RequestHeader("X-User-Id") String userId) {
        PlaceOrderResult result = placeOrderUseCase.placeOrder(orderWebMapper.toPlaceOrderCommand(userId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(orderWebMapper.toPlaceOrderResponse(result));
    }
}
