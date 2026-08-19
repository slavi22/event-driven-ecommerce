package com.eventdriven.projection.product.application.port.out.persistence;

import com.eventdriven.projection.product.application.dto.GetProductResult;

public interface SaveProductQueryPort {
    GetProductResult save(GetProductResult product);
}
