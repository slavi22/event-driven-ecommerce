package com.eventdriven.projection.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetOrderResult(UUID orderId, UUID customerId, BigDecimal totalAmount, String status,
                             Instant createdAt, Instant updatedAt) {
}
