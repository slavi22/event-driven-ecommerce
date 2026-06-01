package com.eventdriven.projection.stock.application.service.query;

import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.exception.StockNotFoundException;
import com.eventdriven.projection.stock.application.port.in.GetStockByProductUseCase;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.query.GetStockQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
class GetStockQueryService implements GetStockByProductUseCase {

    private final GetStockQueryPort getStockQueryPort;

    @Override
    @Transactional(readOnly = true)
    public GetStockResult getStockByProductId(GetStockQuery query) {
        log.info("Getting stock for product with id: {}", query.productId());
        return getStockQueryPort.getStockByProductId(query.productId())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock for product with id " + query.productId() + " not found!"));
    }
}
