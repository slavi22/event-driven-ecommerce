package com.eventdriven.projection.stock.application.port.out.query;

import com.eventdriven.projection.stock.application.dto.GetStockResult;

import java.util.Optional;
import java.util.UUID;

public interface GetStockQueryPort {
    Optional<GetStockResult> getStockByProductId(UUID productId);
}
