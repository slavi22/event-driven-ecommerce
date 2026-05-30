package com.eventdriven.product.adapter.in.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(@NotBlank(message = "Product ID must not be blank")
                                   String productId,
                                   @NotBlank(message = "Name must not be blank")
                                   String name,
                                   @NotBlank(message = "Description must not be blank")
                                   String description,
                                   @NotNull(message = "Product price must not be null")
                                   @DecimalMin(value = "0.00", inclusive = false, message = "Product price must be greater than zero")
                                   BigDecimal price,
                                   @NotBlank(message = "Category must not be blank")
                                   String category) {
}
