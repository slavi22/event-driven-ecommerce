package com.eventdriven.projection.payment.adapter.in.web;

import com.eventdriven.projection.payment.adapter.in.web.dto.response.GetPaymentResponse;
import com.eventdriven.projection.payment.application.port.in.GetPaymentByOrderIdQueryUseCase;
import com.eventdriven.projection.payment.application.query.GetPaymentByOrderIdQuery;
import com.eventdriven.projection.shared.web.swagger.GetPaymentByOrderIdOperation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Payments", description = "Read-only endpoints for querying payment data")
@RestController
@RequestMapping("/api/v1/payments")
@Validated
@RequiredArgsConstructor
class PaymentQueryController {

    private final GetPaymentByOrderIdQueryUseCase getPaymentByOrderIdQueryUseCase;
    private final PaymentWebMapper paymentWebMapper;

    @GetPaymentByOrderIdOperation
    @GetMapping("/order/{orderId}")
    public ResponseEntity<GetPaymentResponse> getPaymentByOrderId(
            @Parameter(description = "UUID of the order to look up payment for", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
            @PathVariable("orderId") @org.hibernate.validator.constraints.UUID String orderId) {
        return ResponseEntity.ok(paymentWebMapper.toGetPaymentResponse(
                getPaymentByOrderIdQueryUseCase.getPaymentByOrderId(
                        new GetPaymentByOrderIdQuery(UUID.fromString(orderId)))));
    }
}
