package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.notification.application.command.OrderPlacedNotificationCommand;
import com.eventdriven.notification.application.port.in.OrderPlacedNotificationUseCase;
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
class OrderPlacedKafkaListener {

    private final OrderPlacedNotificationUseCase orderPlacedNotificationUseCase;

    @KafkaListener(
            topics = "${kafka.topics.order-placed-topic}",
            groupId = "${kafka.config.consumer.groups.notification-order-placed-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderPlacedEventPayload")
    public void onOrderPlaced(@Payload OrderPlacedEventPayload payload) {
        log.info("Notification: received OrderPlaced for order {}", payload.orderId());
        orderPlacedNotificationUseCase.onOrderPlaced(new OrderPlacedNotificationCommand(
                UUID.fromString(payload.orderId()),
                UUID.fromString(payload.customerId()),
                payload.totalAmount()));
    }

    @DltHandler
    public void onOrderPlacedDlt(@Payload OrderPlacedEventPayload payload, Exception ex) {
        log.error("DLT: notification for OrderPlaced {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
