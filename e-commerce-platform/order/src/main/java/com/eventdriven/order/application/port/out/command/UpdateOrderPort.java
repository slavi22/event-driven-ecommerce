package com.eventdriven.order.application.port.out.command;

import com.eventdriven.order.domain.entity.Order;

public interface UpdateOrderPort {

    void update(Order order);
}
