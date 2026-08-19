package com.eventdriven.notification.application.service;

import com.eventdriven.notification.application.command.*;
import com.eventdriven.notification.application.port.out.NotificationPort;
import com.eventdriven.notification.application.port.out.model.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Given an OrderPlacedNotificationCommand, when onOrderPlaced is called, then a notification with customerId, correct subject and body should be sent")
    void testOnOrderPlaced_shouldSendNotificationWithCustomerIdAndOrderDetails() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("199.99");

        // Act
        notificationService.onOrderPlaced(new OrderPlacedNotificationCommand(orderId, customerId, totalAmount));

        // Assert
        verify(notificationPort, times(1)).send(new Notification(
                customerId.toString(),
                "Order received",
                "Your order %s has been received. Total: %s".formatted(orderId, totalAmount)
        ));
    }

    @Test
    @DisplayName("Given an OrderConfirmedNotificationCommand, when onOrderConfirmed is called, then a notification with null recipientId and correct subject and body should be sent")
    void testOnOrderConfirmed_shouldSendNotificationWithNullRecipientAndOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        notificationService.onOrderConfirmed(new OrderConfirmedNotificationCommand(orderId));

        // Assert
        verify(notificationPort, times(1)).send(new Notification(
                null,
                "Order confirmed",
                "Your order %s has been confirmed.".formatted(orderId)
        ));
    }

    @Test
    @DisplayName("Given an OrderCancelledNotificationCommand, when onOrderCancelled is called, then a notification with null recipientId, correct subject and reason should be sent")
    void testOnOrderCancelled_shouldSendNotificationWithNullRecipientAndReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String reason = "Out of stock";

        // Act
        notificationService.onOrderCancelled(new OrderCancelledNotificationCommand(orderId, reason));

        // Assert
        verify(notificationPort, times(1)).send(new Notification(
                null,
                "Order cancelled",
                "Your order %s has been cancelled. Reason: %s".formatted(orderId, reason)
        ));
    }

    @Test
    @DisplayName("Given a PaymentProcessedNotificationCommand, when onPaymentProcessed is called, then a notification with null recipientId, correct subject and amount should be sent")
    void testOnPaymentProcessed_shouldSendNotificationWithNullRecipientAndAmount() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("299.00");

        // Act
        notificationService.onPaymentProcessed(new PaymentProcessedNotificationCommand(orderId, amount));

        // Assert
        verify(notificationPort, times(1)).send(new Notification(
                null,
                "Payment successful",
                "Payment of %s for order %s was successful.".formatted(amount, orderId)
        ));
    }

    @Test
    @DisplayName("Given a PaymentFailedNotificationCommand, when onPaymentFailed is called, then a notification with null recipientId, correct subject and reason should be sent")
    void testOnPaymentFailed_shouldSendNotificationWithNullRecipientAndReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String reason = "Insufficient funds";

        // Act
        notificationService.onPaymentFailed(new PaymentFailedNotificationCommand(orderId, reason));

        // Assert
        verify(notificationPort, times(1)).send(new Notification(
                null,
                "Payment failed",
                "Payment for order %s failed. Reason: %s".formatted(orderId, reason)
        ));
    }
}
