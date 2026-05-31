package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.ReserveStockCommand;
import com.eventdriven.stock.application.port.in.ReserveStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
class ReserveStockService implements ReserveStockUseCase {

    @Override
    public void reserveStock(ReserveStockCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
