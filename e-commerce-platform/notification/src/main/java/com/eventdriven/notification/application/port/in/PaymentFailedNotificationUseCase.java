package com.eventdriven.notification.application.port.in;

import com.eventdriven.notification.application.command.PaymentFailedNotificationCommand;

public interface PaymentFailedNotificationUseCase {
    void onPaymentFailed(PaymentFailedNotificationCommand command);
}
