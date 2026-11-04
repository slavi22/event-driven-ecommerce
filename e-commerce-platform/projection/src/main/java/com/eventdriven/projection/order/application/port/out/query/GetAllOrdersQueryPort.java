package com.eventdriven.projection.order.application.port.out.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

import java.util.List;
import java.util.UUID;

public interface GetAllOrdersQueryPort {
    List<GetOrderResult> getAllOrders(UUID customerId);
}
