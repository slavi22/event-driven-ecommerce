package com.eventdriven.stock.application.port.out.command;

import com.eventdriven.stock.domain.entity.Stock;

import java.util.Optional;
import java.util.UUID;

public interface GetStockCommandPort {
    Optional<Stock> getStockByProductId(UUID productId);
}
