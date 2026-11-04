package com.eventdriven.notification.application.port.in;

import com.eventdriven.notification.application.command.OrderConfirmedNotificationCommand;

public interface OrderConfirmedNotificationUseCase {
    void onOrderConfirmed(OrderConfirmedNotificationCommand command);
}
