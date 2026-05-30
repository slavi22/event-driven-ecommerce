package com.eventdriven.product.application.command;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DeleteProductCommand(@NotBlank(message = "Product ID is required") UUID productId) {
}
