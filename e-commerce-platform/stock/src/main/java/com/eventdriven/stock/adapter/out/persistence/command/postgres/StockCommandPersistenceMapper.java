package com.eventdriven.stock.adapter.out.persistence.command.postgres;

import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.valueobject.Quantity;
import com.eventdriven.stock.domain.valueobject.StockId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface StockCommandPersistenceMapper {

    default Stock toStock(StockEntity entity) {
        return Stock.reconstitute(
                new StockId(entity.getId()),
                entity.getProductId(),
                Quantity.of(entity.getQuantity()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Mapping(target = "id", expression = "java(stock.getId().getValue())")
    @Mapping(target = "quantity", expression = "java(stock.getQuantity().getValue())")
    StockEntity toStockEntity(Stock stock);
}
