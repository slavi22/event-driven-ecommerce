package com.eventdriven.stock.application.port.out.command;

import com.eventdriven.stock.domain.entity.Stock;

public interface SaveStockPort {
    Stock save(Stock stock);
}
