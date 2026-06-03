package com.eventdriven.projection.order.adapter.in.web;

import com.eventdriven.projection.order.adapter.in.web.dto.response.GetOrderResponse;
import com.eventdriven.projection.order.application.dto.GetOrderResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderWebMapper {
    GetOrderResponse toGetOrderResponse(GetOrderResult result);
}
