package com.eventdriven.projection.order.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GetOrderResponse(UUID orderId, UUID customerId, BigDecimal totalAmount, String status,
                               Instant createdAt, Instant updatedAt) {
}
