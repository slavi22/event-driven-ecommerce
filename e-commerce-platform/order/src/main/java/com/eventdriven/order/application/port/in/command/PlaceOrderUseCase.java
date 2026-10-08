package com.eventdriven.order.application.port.in.command;

import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;

public interface PlaceOrderUseCase {
    PlaceOrderResult placeOrder(PlaceOrderCommand command);
}
