package com.eventdriven.notification.application.port.in;

import com.eventdriven.notification.application.command.PaymentProcessedNotificationCommand;

public interface PaymentProcessedNotificationUseCase {
    void onPaymentProcessed(PaymentProcessedNotificationCommand command);
}
