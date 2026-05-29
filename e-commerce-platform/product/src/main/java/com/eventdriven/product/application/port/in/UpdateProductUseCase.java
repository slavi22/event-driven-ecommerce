package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.command.UpdateProductCommand;
import com.eventdriven.product.application.dto.UpdateProductResult;

public interface UpdateProductUseCase {
    UpdateProductResult updateProduct(UpdateProductCommand command);
}
