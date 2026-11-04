package com.eventdriven.order.application.port.out.command;

import com.eventdriven.order.domain.entity.Order;

import java.util.UUID;

public interface GetOrderPort {

    Order getOrder(UUID orderId);
}
