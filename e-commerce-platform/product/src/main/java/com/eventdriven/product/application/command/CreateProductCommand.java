package com.eventdriven.product.application.command;

import com.eventdriven.product.domain.valueobject.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductCommand(
        @NotBlank(message = "Product name must not be blank")
        String name,
        @NotBlank(message = "Product description must not be blank")
        String description,
        @NotNull(message = "Product price must not be null")
        @DecimalMin(value = "0.00", inclusive = false, message = "Product price must be greater than zero")
        BigDecimal price,
        @NotNull(message = "Product category must not be null")
        ProductCategory category) {
}
