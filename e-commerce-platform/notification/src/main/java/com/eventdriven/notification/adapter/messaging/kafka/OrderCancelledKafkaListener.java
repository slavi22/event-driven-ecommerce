package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderCancelledEventPayload;
import com.eventdriven.notification.application.command.OrderCancelledNotificationCommand;
import com.eventdriven.notification.application.port.in.OrderCancelledNotificationUseCase;
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
class OrderCancelledKafkaListener {

    private final OrderCancelledNotificationUseCase orderCancelledNotificationUseCase;

    @KafkaListener(
            topics = "${kafka.topics.order-cancelled-topic}",
            groupId = "${kafka.config.consumer.groups.notification-order-cancelled-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderCancelledEventPayload")
    public void onOrderCancelled(@Payload OrderCancelledEventPayload payload) {
        log.info("Notification: received OrderCancelled for order {}", payload.orderId());
        orderCancelledNotificationUseCase.onOrderCancelled(new OrderCancelledNotificationCommand(
                UUID.fromString(payload.orderId()),
                payload.reason()));
    }

    @DltHandler
    public void onOrderCancelledDlt(@Payload OrderCancelledEventPayload payload, Exception ex) {
        log.error("DLT: notification for OrderCancelled {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
