package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.dto.CreateProductResult;

public interface CreateProductUseCase {
    CreateProductResult createProduct(CreateProductCommand command);
}
