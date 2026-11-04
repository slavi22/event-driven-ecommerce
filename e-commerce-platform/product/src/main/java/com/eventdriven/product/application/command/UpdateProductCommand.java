package com.eventdriven.product.application.command;

import com.eventdriven.contracts.product.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductCommand(
        String productId,
        String name,
        String description,
        BigDecimal price,
        ProductCategory category) {
}
