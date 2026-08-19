package com.eventdriven.projection.payment.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Read model representing a payment associated with an order")
public record GetPaymentResponse(
        @Schema(description = "UUID of the order this payment belongs to", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
        UUID orderId,

        @Schema(description = "Payment amount in USD", example = "299.98")
        BigDecimal amount,

        @Schema(description = "Current status of the payment", example = "PROCESSED",
                allowableValues = {"PENDING", "PROCESSED", "FAILED"})
        String status,

        @Schema(description = "Reason for payment failure, if applicable", example = "Insufficient funds", nullable = true)
        String failureReason,

        @Schema(description = "Timestamp when the payment was created", example = "2026-06-05T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the payment was last updated", example = "2026-06-05T10:35:00Z")
        Instant updatedAt) {
}
