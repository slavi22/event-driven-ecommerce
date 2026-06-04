package com.eventdriven.notification.application.port.in;

import com.eventdriven.notification.application.command.OrderPlacedNotificationCommand;

public interface OrderPlacedNotificationUseCase {
    void onOrderPlaced(OrderPlacedNotificationCommand command);
}
