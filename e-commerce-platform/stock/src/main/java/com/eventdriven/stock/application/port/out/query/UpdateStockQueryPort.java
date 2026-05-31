package com.eventdriven.stock.application.port.out.query;

import com.eventdriven.stock.domain.entity.Stock;

public interface UpdateStockQueryPort {
    Stock update(Stock stock);
}
