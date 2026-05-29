package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.dto.CreateProductResult;

import java.util.List;

public interface GetAllProductsQueryUseCase {
    List<CreateProductResult> getAllProducts();
}
