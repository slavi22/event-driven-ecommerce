package com.eventdriven.stock.application.port.in;

import com.eventdriven.stock.application.command.ReserveStockCommand;

public interface ReserveStockUseCase {
    void reserveStock(ReserveStockCommand command);
}
