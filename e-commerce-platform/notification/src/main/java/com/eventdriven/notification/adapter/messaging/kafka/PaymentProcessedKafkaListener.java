package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.notification.application.command.PaymentProcessedNotificationCommand;
import com.eventdriven.notification.application.port.in.PaymentProcessedNotificationUseCase;
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
class PaymentProcessedKafkaListener {

    private final PaymentProcessedNotificationUseCase paymentProcessedNotificationUseCase;

    @KafkaListener(
            topics = "${kafka.topics.payment-processed-topic}",
            groupId = "${kafka.config.consumer.groups.notification-payment-processed-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload")
    public void onPaymentProcessed(@Payload PaymentProcessedEventPayload payload) {
        log.info("Notification: received PaymentProcessed for order {}", payload.orderId());
        paymentProcessedNotificationUseCase.onPaymentProcessed(new PaymentProcessedNotificationCommand(
                UUID.fromString(payload.orderId()),
                payload.amount()));
    }

    @DltHandler
    public void onPaymentProcessedDlt(@Payload PaymentProcessedEventPayload payload, Exception ex) {
        log.error("DLT: notification for PaymentProcessed {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
