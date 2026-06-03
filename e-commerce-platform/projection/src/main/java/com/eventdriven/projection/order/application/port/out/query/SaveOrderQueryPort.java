package com.eventdriven.projection.order.application.port.out.query;

import com.eventdriven.projection.order.application.dto.GetOrderResult;

public interface SaveOrderQueryPort {
    GetOrderResult save(GetOrderResult result);
}
