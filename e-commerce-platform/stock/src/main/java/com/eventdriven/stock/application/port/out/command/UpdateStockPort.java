package com.eventdriven.stock.application.port.out.command;

import com.eventdriven.stock.domain.entity.Stock;

public interface UpdateStockPort {
    Stock update(Stock stock);
}
