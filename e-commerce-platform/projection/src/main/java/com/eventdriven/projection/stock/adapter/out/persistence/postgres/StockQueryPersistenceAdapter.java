package com.eventdriven.projection.stock.adapter.out.persistence.postgres;

import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.port.out.query.SaveStockQueryPort;
import com.eventdriven.projection.stock.application.port.out.query.UpdateStockQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class StockQueryPersistenceAdapter implements GetStockQueryPort, SaveStockQueryPort, UpdateStockQueryPort {

    private final StockReadJpaRepository stockReadJpaRepository;
    private final StockQueryPersistenceMapper stockQueryPersistenceMapper;

    @Override
    public Optional<GetStockResult> getStockByProductId(UUID productId) {
        return stockReadJpaRepository.findByProductId(productId)
                .map(stockQueryPersistenceMapper::toGetStockResult);
    }

    @Override
    public GetStockResult save(GetStockResult result) {
        StockReadEntity entity = stockQueryPersistenceMapper.toStockReadEntity(result);
        return stockQueryPersistenceMapper.toGetStockResult(stockReadJpaRepository.save(entity));
    }

    @Override
    public GetStockResult update(GetStockResult result) {
        StockReadEntity entity = stockReadJpaRepository.findByProductId(result.productId())
                .orElseThrow(() -> new IllegalStateException(
                        "Stock read entity not found for product " + result.productId()));
        entity.setQuantity(result.quantity());
        entity.setUpdatedAt(result.updatedAt());
        return stockQueryPersistenceMapper.toGetStockResult(stockReadJpaRepository.save(entity));
    }
}
