package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.dto.CreateProductResult;
import com.eventdriven.product.application.query.GetProductQuery;

public interface GetProductQueryUseCase {
    CreateProductResult getProductByProductId(GetProductQuery query);
}
