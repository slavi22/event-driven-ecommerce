package com.eventdriven.projection.order.application.port.in;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

import java.util.List;

public interface GetAllOrdersQueryUseCase {
    List<GetOrderResult> getAllOrders();
}
