package com.eventdriven.projection.product.application.port.out.persistence;

import com.eventdriven.projection.product.application.dto.GetProductResult;

import java.util.List;

public interface GetAllProductsQueryPort {
    List<GetProductResult> getAllProducts();
}
