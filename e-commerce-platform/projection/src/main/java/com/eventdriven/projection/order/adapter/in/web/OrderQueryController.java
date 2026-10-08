package com.eventdriven.projection.order.adapter.in.web;

import com.eventdriven.projection.order.adapter.in.web.dto.response.GetOrderResponse;
import com.eventdriven.projection.order.application.port.in.GetAllOrdersQueryUseCase;
import com.eventdriven.projection.order.application.port.in.GetOrderQueryUseCase;
import com.eventdriven.projection.order.application.query.GetAllOrdersQuery;
import com.eventdriven.projection.order.application.query.GetOrderQuery;
import com.eventdriven.projection.shared.web.swagger.GetAllOrdersOperation;
import com.eventdriven.projection.shared.web.swagger.GetOrderOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Read-only endpoints for querying order data")
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@RequiredArgsConstructor
class OrderQueryController {

    private final GetOrderQueryUseCase getOrderQueryUseCase;
    private final GetAllOrdersQueryUseCase getAllOrdersQueryUseCase;
    private final OrderWebMapper orderWebMapper;

    @GetOrderOperation
    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(
            @Parameter(description = "UUID of the order", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
            @PathVariable("orderId") @org.hibernate.validator.constraints.UUID String orderId) {
        return ResponseEntity.ok(orderWebMapper.toGetOrderResponse(
                getOrderQueryUseCase.getOrder(new GetOrderQuery(UUID.fromString(orderId)))));
    }

    @GetAllOrdersOperation
    @GetMapping
    public ResponseEntity<List<GetOrderResponse>> getAllOrders(
            @Parameter(description = "ID of the authenticated user, injected by the API gateway", example = "user-123", required = true)
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(getAllOrdersQueryUseCase.getAllOrders(new GetAllOrdersQuery(UUID.fromString(userId))).stream()
                .map(orderWebMapper::toGetOrderResponse)
                .toList());
    }
}
