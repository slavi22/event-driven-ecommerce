package com.eventdriven.product.adapter.in.web.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record CreateProductResponse(
        String name,
        String description,
        BigDecimal price,
        String category,
        String status,
        ZonedDateTime createdAt) {
}
