package com.eventdriven.notification.application.service;

import com.eventdriven.notification.application.command.*;
import com.eventdriven.notification.application.port.in.*;
import com.eventdriven.notification.application.port.out.NotificationPort;
import com.eventdriven.notification.application.port.out.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
class NotificationService implements
        OrderPlacedNotificationUseCase,
        OrderConfirmedNotificationUseCase,
        OrderCancelledNotificationUseCase,
        PaymentProcessedNotificationUseCase,
        PaymentFailedNotificationUseCase {

    private final NotificationPort notificationPort;

    @Override
    public void onOrderPlaced(OrderPlacedNotificationCommand cmd) {
        notificationPort.send(new Notification(
                cmd.customerId().toString(),
                "Order received",
                "Your order %s has been received. Total: %s".formatted(cmd.orderId(), cmd.totalAmount())
        ));
        log.info("Notification sent → OrderPlaced for order {}", cmd.orderId());
    }

    @Override
    public void onOrderConfirmed(OrderConfirmedNotificationCommand cmd) {
        notificationPort.send(new Notification(
                null,
                "Order confirmed",
                "Your order %s has been confirmed.".formatted(cmd.orderId())
        ));
        log.info("Notification sent → OrderConfirmed for order {}", cmd.orderId());
    }

    @Override
    public void onOrderCancelled(OrderCancelledNotificationCommand cmd) {
        notificationPort.send(new Notification(
                null,
                "Order cancelled",
                "Your order %s has been cancelled. Reason: %s".formatted(cmd.orderId(), cmd.reason())
        ));
        log.info("Notification sent → OrderCancelled for order {}", cmd.orderId());
    }

    @Override
    public void onPaymentProcessed(PaymentProcessedNotificationCommand cmd) {
        notificationPort.send(new Notification(
                null,
                "Payment successful",
                "Payment of %s for order %s was successful.".formatted(cmd.amount(), cmd.orderId())
        ));
        log.info("Notification sent → PaymentProcessed for order {}", cmd.orderId());
    }

    @Override
    public void onPaymentFailed(PaymentFailedNotificationCommand cmd) {
        notificationPort.send(new Notification(
                null,
                "Payment failed",
                "Payment for order %s failed. Reason: %s".formatted(cmd.orderId(), cmd.reason())
        ));
        log.info("Notification sent → PaymentFailed for order {}", cmd.orderId());
    }
}
