package com.eventdriven.stock.application.service.query;

import com.eventdriven.stock.application.dto.GetStockResult;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.mapper.StockApplicationMapper;
import com.eventdriven.stock.application.port.in.GetStockByProductUseCase;
import com.eventdriven.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.stock.application.query.GetStockQuery;
import com.eventdriven.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
class GetStockQueryService implements GetStockByProductUseCase {

    private final GetStockQueryPort getStockQueryPort;
    private final StockApplicationMapper stockApplicationMapper;

    @Override
    public GetStockResult getStockByProductId(GetStockQuery query) {
        log.info("Getting stock for product with id: {}", query.productId());
        Stock stock =
                getStockQueryPort.getStockByProductId(query.productId()).orElseThrow(() -> new StockNotFoundException(
                        "Stock for product with id " + query.productId() + " not found!"));
        log.info("Stock found for product with id: {} returning response", query.productId());
        return stockApplicationMapper.toGetStockResult(stock);
    }

}
