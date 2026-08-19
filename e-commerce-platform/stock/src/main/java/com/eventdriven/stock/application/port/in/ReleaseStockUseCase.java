package com.eventdriven.stock.application.port.in;

import com.eventdriven.stock.application.command.ReleaseStockCommand;

public interface ReleaseStockUseCase {
    void releaseStock(ReleaseStockCommand command);
}
