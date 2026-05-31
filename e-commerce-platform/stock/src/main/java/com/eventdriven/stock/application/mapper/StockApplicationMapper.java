package com.eventdriven.stock.application.mapper;

import com.eventdriven.stock.application.dto.GetStockResult;
import com.eventdriven.stock.domain.entity.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockApplicationMapper {
    GetStockResult toGetStockResult(Stock stock);
}