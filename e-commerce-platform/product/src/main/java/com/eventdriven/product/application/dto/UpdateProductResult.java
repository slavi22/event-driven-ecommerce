package com.eventdriven.product.application.dto;

import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;

import java.math.BigDecimal;

public record UpdateProductResult(
        String name,
        String description,
        BigDecimal price,
        ProductCategory category,
        ProductStatus status) {
}
