package com.eventdriven.projection.product.application.port.in.query;

import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.query.GetProductQuery;

public interface GetProductQueryUseCase {
    GetProductResult getProductByProductId(GetProductQuery query);
}
