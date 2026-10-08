package com.eventdriven.projection.payment.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetPaymentResult(UUID orderId, BigDecimal amount, String status,
                               String failureReason, Instant createdAt, Instant updatedAt) {
}
