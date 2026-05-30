package com.eventdriven.product.application.port.in.query;

import com.eventdriven.product.application.dto.GetProductResult;
import com.eventdriven.product.application.query.GetProductQuery;

public interface GetProductQueryUseCase {
    GetProductResult getProductByProductId(GetProductQuery query);
}
