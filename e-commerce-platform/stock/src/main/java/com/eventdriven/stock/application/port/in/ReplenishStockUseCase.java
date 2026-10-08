package com.eventdriven.stock.application.port.in;

import com.eventdriven.stock.application.command.ReplenishStockCommand;
import com.eventdriven.stock.application.dto.ReplenishStockResult;

public interface ReplenishStockUseCase {
    ReplenishStockResult replenishStock(ReplenishStockCommand command);
}
