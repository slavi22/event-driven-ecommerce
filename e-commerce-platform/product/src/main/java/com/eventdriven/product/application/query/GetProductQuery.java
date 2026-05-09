package com.eventdriven.product.application.query;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record GetProductQuery(@NotBlank(message = "Product ID is required") UUID productId) {
}
