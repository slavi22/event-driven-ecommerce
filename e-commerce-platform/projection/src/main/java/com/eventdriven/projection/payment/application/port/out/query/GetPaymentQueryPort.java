package com.eventdriven.projection.payment.application.port.out.query;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;

import java.util.Optional;
import java.util.UUID;

public interface GetPaymentQueryPort {
    Optional<GetPaymentResult> getPaymentByOrderId(UUID orderId);
}
