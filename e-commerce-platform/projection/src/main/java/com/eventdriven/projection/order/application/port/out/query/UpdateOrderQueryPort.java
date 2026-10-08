package com.eventdriven.projection.order.application.port.out.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

public interface UpdateOrderQueryPort {
    GetOrderResult update(GetOrderResult result);
}
