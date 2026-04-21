package com.eventdriven.product.application.port.in;

import com.eventdriven.product.application.dto.ProductResponse;
import com.eventdriven.product.application.query.GetProductQuery;

public interface GetProductUseCase {
    ProductResponse getProduct(GetProductQuery query);
}
