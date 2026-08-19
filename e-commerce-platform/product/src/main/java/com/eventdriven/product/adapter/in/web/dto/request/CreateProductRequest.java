package com.eventdriven.product.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request body for creating a new product")
public record CreateProductRequest(
        @Schema(description = "Name of the product", example = "Wireless Headphones")
        @NotBlank(message = "Name must not be blank")
        String name,

        @Schema(description = "Detailed description of the product", example = "Premium noise-cancelling wireless headphones with 30-hour battery life")
        @NotBlank(message = "Description must not be blank")
        String description,

        @Schema(description = "Price of the product in USD (must be greater than 0)", example = "149.99")
        @NotNull(message = "Product price must not be null")
        @DecimalMin(value = "0.00", inclusive = false, message = "Product price must be greater than zero")
        BigDecimal price,

        @Schema(description = "Product category", example = "ELECTRONICS",
                allowableValues = {"ELECTRONICS", "CLOTHING", "BOOKS", "FOOD_AND_BEVERAGES",
                                   "HOME_AND_GARDEN", "SPORTS", "TOYS", "HEALTH_AND_BEAUTY"})
        @NotBlank(message = "Category must not be blank")
        String category,

        @Schema(description = "Initial stock quantity (must be at least 1)", example = "100")
        @NotNull(message = "Initial quantity must not be null")
        @Min(value = 1, message = "Initial quantity must be at least 1")
        Integer initialQuantity) {
}
