package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.notification.application.command.PaymentFailedNotificationCommand;
import com.eventdriven.notification.application.port.in.PaymentFailedNotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class PaymentFailedKafkaListener {

    private final PaymentFailedNotificationUseCase paymentFailedNotificationUseCase;

    @KafkaListener(
            topics = "${kafka.topics.payment-failed-topic}",
            groupId = "${kafka.config.consumer.groups.notification-payment-failed-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.payment.event.PaymentFailedEventPayload")
    public void onPaymentFailed(@Payload PaymentFailedEventPayload payload) {
        log.info("Notification: received PaymentFailed for order {}", payload.orderId());
        paymentFailedNotificationUseCase.onPaymentFailed(new PaymentFailedNotificationCommand(
                UUID.fromString(payload.orderId()),
                payload.reason()));
    }

    @DltHandler
    public void onPaymentFailedDlt(@Payload PaymentFailedEventPayload payload, Exception ex) {
        log.error("DLT: notification for PaymentFailed {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
