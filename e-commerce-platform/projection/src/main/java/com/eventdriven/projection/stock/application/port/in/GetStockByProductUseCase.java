package com.eventdriven.projection.stock.application.port.in;

import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.query.GetStockQuery;

public interface GetStockByProductUseCase {
    GetStockResult getStockByProductId(GetStockQuery query);
}
