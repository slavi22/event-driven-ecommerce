package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderConfirmedEventPayload;
import com.eventdriven.notification.application.command.OrderConfirmedNotificationCommand;
import com.eventdriven.notification.application.port.in.OrderConfirmedNotificationUseCase;
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
class OrderConfirmedKafkaListener {

    private final OrderConfirmedNotificationUseCase orderConfirmedNotificationUseCase;

    @KafkaListener(
            topics = "${kafka.topics.order-confirmed-topic}",
            groupId = "${kafka.config.consumer.groups.notification-order-confirmed-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderConfirmedEventPayload")
    public void onOrderConfirmed(@Payload OrderConfirmedEventPayload payload) {
        log.info("Notification: received OrderConfirmed for order {}", payload.orderId());
        orderConfirmedNotificationUseCase.onOrderConfirmed(new OrderConfirmedNotificationCommand(
                UUID.fromString(payload.orderId())));
    }

    @DltHandler
    public void onOrderConfirmedDlt(@Payload OrderConfirmedEventPayload payload, Exception ex) {
        log.error("DLT: notification for OrderConfirmed {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
