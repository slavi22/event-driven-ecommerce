package com.eventdriven.stock.application.port.in;

import com.eventdriven.stock.application.command.ReplenishStockCommand;

public interface ReplenishStockUseCase {
    void replenishStock(ReplenishStockCommand command);
}
