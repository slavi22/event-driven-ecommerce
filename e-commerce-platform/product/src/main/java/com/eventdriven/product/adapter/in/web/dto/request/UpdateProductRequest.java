package com.eventdriven.product.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request body for updating an existing product")
public record UpdateProductRequest(
        @Schema(description = "UUID of the product to update", example = "a3f2c1d4-5b6e-7f8a-9b0c-1d2e3f4a5b6c")
        @NotBlank(message = "Product ID must not be blank")
        String productId,

        @Schema(description = "Updated product name", example = "Wireless Headphones Pro")
        @NotBlank(message = "Name must not be blank")
        String name,

        @Schema(description = "Updated product description", example = "Premium noise-cancelling headphones with 40-hour battery life")
        @NotBlank(message = "Description must not be blank")
        String description,

        @Schema(description = "Updated price in USD (must be greater than 0)", example = "179.99")
        @NotNull(message = "Product price must not be null")
        @DecimalMin(value = "0.00", inclusive = false, message = "Product price must be greater than zero")
        BigDecimal price,

        @Schema(description = "Updated product category", example = "ELECTRONICS",
                allowableValues = {"ELECTRONICS", "CLOTHING", "BOOKS", "FOOD_AND_BEVERAGES",
                                   "HOME_AND_GARDEN", "SPORTS", "TOYS", "HEALTH_AND_BEAUTY"})
        @NotBlank(message = "Category must not be blank")
        String category) {
}
