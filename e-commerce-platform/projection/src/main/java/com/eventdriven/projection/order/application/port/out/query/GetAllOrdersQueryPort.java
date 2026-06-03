package com.eventdriven.projection.order.application.port.out.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

import java.util.List;

public interface GetAllOrdersQueryPort {
    List<GetOrderResult> getAllOrders();
}
