package com.eventdriven.projection.payment.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetPaymentResponse(UUID orderId, BigDecimal amount, String status,
                                 String failureReason, Instant createdAt, Instant updatedAt) {
}
