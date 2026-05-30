package com.eventdriven.product.application.port.in.command;

import com.eventdriven.product.application.command.DeleteProductCommand;

public interface DeleteProductUseCase {
    void deleteProduct(DeleteProductCommand command);
}
