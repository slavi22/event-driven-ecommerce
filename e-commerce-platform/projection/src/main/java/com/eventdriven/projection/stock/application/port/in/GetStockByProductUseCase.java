package com.eventdriven.projection.stock.application.port.in;

import com.eventdriven.stock.application.dto.GetStockResult;
import com.eventdriven.stock.application.query.GetStockQuery;

public interface GetStockByProductUseCase {
    GetStockResult getStockByProductId(GetStockQuery query);
}
