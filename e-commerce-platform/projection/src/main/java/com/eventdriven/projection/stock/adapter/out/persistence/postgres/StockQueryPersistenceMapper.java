package com.eventdriven.projection.stock.adapter.out.persistence.postgres;

import com.eventdriven.projection.stock.application.dto.GetStockResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface StockQueryPersistenceMapper {

    @Mapping(target = "stockId", source = "id")
    GetStockResult toGetStockResult(StockReadEntity entity);

    @Mapping(target = "id", source = "stockId")
    StockReadEntity toStockReadEntity(GetStockResult result);
}
