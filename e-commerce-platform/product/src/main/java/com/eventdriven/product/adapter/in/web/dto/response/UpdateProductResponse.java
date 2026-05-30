package com.eventdriven.product.adapter.in.web.dto.response;

import java.math.BigDecimal;

public record UpdateProductResponse(
        String name,
        String description,
        BigDecimal price,
        String category,
        String status) {
}
