package com.eventdriven.stock.application.port.in;

import com.eventdriven.stock.application.command.InitializeStockCommand;

public interface InitializeStockUseCase {
    void initializeStock(InitializeStockCommand command);
}
