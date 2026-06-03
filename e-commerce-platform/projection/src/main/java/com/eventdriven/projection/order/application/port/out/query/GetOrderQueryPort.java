package com.eventdriven.projection.order.application.port.out.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

import java.util.Optional;
import java.util.UUID;

public interface GetOrderQueryPort {
    Optional<GetOrderResult> getOrderById(UUID orderId);
}
