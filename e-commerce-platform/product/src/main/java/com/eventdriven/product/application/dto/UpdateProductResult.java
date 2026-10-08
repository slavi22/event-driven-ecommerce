package com.eventdriven.product.application.dto;

import com.eventdriven.contracts.product.enums.ProductCategory;
import com.eventdriven.contracts.product.enums.ProductStatus;

import java.math.BigDecimal;

public record UpdateProductResult(
        String name,
        String description,
        BigDecimal price,
        ProductCategory category,
        ProductStatus status) {
}
