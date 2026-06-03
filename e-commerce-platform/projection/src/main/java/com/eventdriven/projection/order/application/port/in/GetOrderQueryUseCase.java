package com.eventdriven.projection.order.application.port.in;

import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.query.GetOrderQuery;

public interface GetOrderQueryUseCase {
    GetOrderResult getOrder(GetOrderQuery query);
}
