package com.eventdriven.notification.application.port.in;

import com.eventdriven.notification.application.command.OrderCancelledNotificationCommand;

public interface OrderCancelledNotificationUseCase {
    void onOrderCancelled(OrderCancelledNotificationCommand command);
}
