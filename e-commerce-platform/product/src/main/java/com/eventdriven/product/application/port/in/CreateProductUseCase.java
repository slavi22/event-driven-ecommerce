package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.dto.ProductResponse;

public interface CreateProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);
}
