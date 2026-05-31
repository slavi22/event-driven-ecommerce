package com.eventdriven.stock.application.mapper;

import com.eventdriven.stock.application.dto.GetStockResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockApplicationMapper {
    // TODO: mapper for the command side
}