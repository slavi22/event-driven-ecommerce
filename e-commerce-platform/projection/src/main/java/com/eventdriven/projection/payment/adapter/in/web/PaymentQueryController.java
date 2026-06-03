package com.eventdriven.projection.payment.adapter.in.web;

import com.eventdriven.projection.payment.adapter.in.web.dto.response.GetPaymentResponse;
import com.eventdriven.projection.payment.application.port.in.GetPaymentByOrderIdQueryUseCase;
import com.eventdriven.projection.payment.application.query.GetPaymentByOrderIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Validated
@RequiredArgsConstructor
class PaymentQueryController {

    private final GetPaymentByOrderIdQueryUseCase getPaymentByOrderIdQueryUseCase;
    private final PaymentWebMapper paymentWebMapper;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<GetPaymentResponse> getPaymentByOrderId(
            @PathVariable @org.hibernate.validator.constraints.UUID String orderId) {
        return ResponseEntity.ok(paymentWebMapper.toGetPaymentResponse(
                getPaymentByOrderIdQueryUseCase.getPaymentByOrderId(
                        new GetPaymentByOrderIdQuery(UUID.fromString(orderId)))));
    }
}
