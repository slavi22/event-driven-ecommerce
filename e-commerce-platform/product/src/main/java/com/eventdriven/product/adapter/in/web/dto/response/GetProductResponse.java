package com.eventdriven.product.adapter.in.web.dto.response;

import java.math.BigDecimal;

public record GetProductResponse(
        String name,
        String description,
        BigDecimal price,
        String category) {
}
