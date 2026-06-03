package com.eventdriven.projection.order.application.query;

import java.util.UUID;

public record GetOrderQuery(UUID orderId) {
}
