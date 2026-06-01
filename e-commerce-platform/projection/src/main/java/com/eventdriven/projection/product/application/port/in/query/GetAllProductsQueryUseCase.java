package com.eventdriven.projection.product.application.port.in.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;

import java.util.List;

public interface GetAllProductsQueryUseCase {
    List<GetProductResult> getAllProducts();
}
