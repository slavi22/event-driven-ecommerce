package com.eventdriven.product.application.command;

import com.eventdriven.contracts.product.enums.ProductCategory;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        String description,
        BigDecimal price,
        ProductCategory category,
        Integer initialQuantity) {
}
