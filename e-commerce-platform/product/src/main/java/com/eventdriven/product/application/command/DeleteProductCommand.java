package com.eventdriven.product.application.command;

import java.util.UUID;

public record DeleteProductCommand(UUID productId) {
}
