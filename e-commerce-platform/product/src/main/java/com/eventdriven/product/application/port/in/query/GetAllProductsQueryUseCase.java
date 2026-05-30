package com.eventdriven.product.application.port.in.query;

import com.eventdriven.product.application.dto.GetProductResult;

import java.util.List;

public interface GetAllProductsQueryUseCase {
    List<GetProductResult> getAllProducts();
}
