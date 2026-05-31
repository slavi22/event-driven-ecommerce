package com.eventdriven.stock.application.port.out.query;

import com.eventdriven.stock.domain.entity.Stock;

import java.util.Optional;
import java.util.UUID;

public interface GetStockQueryPort {
    Optional<Stock> getStockByProductId(UUID productId);
}
