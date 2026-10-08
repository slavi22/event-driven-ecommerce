package com.eventdriven.projection.stock.application.port.out.query;

import com.eventdriven.projection.stock.application.dto.GetStockResult;

public interface SaveStockQueryPort {
    GetStockResult save(GetStockResult result);
}
