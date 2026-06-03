package com.eventdriven.projection.order.adapter.in.web;

import com.eventdriven.projection.order.adapter.in.web.dto.response.GetOrderResponse;
import com.eventdriven.projection.order.application.port.in.GetAllOrdersQueryUseCase;
import com.eventdriven.projection.order.application.port.in.GetOrderQueryUseCase;
import com.eventdriven.projection.order.application.query.GetOrderQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
@RequiredArgsConstructor
class OrderQueryController {

    private final GetOrderQueryUseCase getOrderQueryUseCase;
    private final GetAllOrdersQueryUseCase getAllOrdersQueryUseCase;
    private final OrderWebMapper orderWebMapper;

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrder(
            @PathVariable @org.hibernate.validator.constraints.UUID String orderId) {
        return ResponseEntity.ok(orderWebMapper.toGetOrderResponse(
                getOrderQueryUseCase.getOrder(new GetOrderQuery(UUID.fromString(orderId)))));
    }

    @GetMapping
    public ResponseEntity<List<GetOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(getAllOrdersQueryUseCase.getAllOrders().stream()
                .map(orderWebMapper::toGetOrderResponse)
                .toList());
    }
}
