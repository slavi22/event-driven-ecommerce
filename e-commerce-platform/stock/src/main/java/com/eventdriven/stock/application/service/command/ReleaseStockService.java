package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.ReleaseStockCommand;
import com.eventdriven.stock.application.port.in.ReleaseStockUseCase;
import org.springframework.stereotype.Service;

@Service
class ReleaseStockService implements ReleaseStockUseCase {

    @Override
    public void releaseStock(ReleaseStockCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
