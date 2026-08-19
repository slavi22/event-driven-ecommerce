package com.eventdriven.stock.adapter.out.persistence.command.postgres;

import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.SaveStockPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class StockCommandPersistenceAdapter implements GetStockCommandPort, SaveStockPort, UpdateStockPort {

    private final StockJpaRepository stockJpaRepository;
    private final StockCommandPersistenceMapper stockCommandPersistenceMapper;

    @Override
    public Optional<Stock> getStockByProductId(UUID productId) {
        return stockJpaRepository.findByProductId(productId)
                                 .map(stockCommandPersistenceMapper::toStock);
    }

    @Override
    public Stock save(Stock stock) {
        StockEntity entity = stockCommandPersistenceMapper.toStockEntity(stock);
        return stockCommandPersistenceMapper.toStock(stockJpaRepository.save(entity));
    }

    @Override
    public Stock update(Stock stock) {
        StockEntity entity = stockJpaRepository.findByProductId(stock.getProductId())
                                               .orElseThrow(() -> new IllegalStateException(
                                                       "Stock for product " + stock.getProductId() + " not found"));
        entity.setQuantity(stock.getQuantity().getValue());
        entity.setUpdatedAt(stock.getUpdatedAt());
        return stockCommandPersistenceMapper.toStock(stockJpaRepository.save(entity));
    }
}
