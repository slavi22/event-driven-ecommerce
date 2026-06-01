package com.eventdriven.projection.stock.application.port.out.query;

import com.eventdriven.projection.stock.application.dto.GetStockResult;

public interface UpdateStockQueryPort {
    GetStockResult update(GetStockResult result);
}
