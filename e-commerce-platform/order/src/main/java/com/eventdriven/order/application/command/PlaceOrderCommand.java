package com.eventdriven.order.application.command;

import java.util.List;
import java.util.UUID;

public record PlaceOrderCommand(UUID customerId, List<OrderItemCommand> items) {
}
