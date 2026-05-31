package com.eventdriven.projection.product.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record GetProductResponse(String productId,
                                 String name,
                                 String description,
                                 BigDecimal price,
                                 String category,
                                 String status,
                                 Instant createdAt,
                                 Instant updatedAt) {
}
