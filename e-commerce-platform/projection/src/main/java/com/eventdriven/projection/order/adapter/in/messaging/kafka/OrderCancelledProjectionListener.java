package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderCancelledEventPayload;
import com.eventdriven.projection.order.application.dto.GetOrderResult;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.port.out.query.UpdateOrderQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
class OrderCancelledProjectionListener {

    private final GetOrderQueryPort getOrderQueryPort;
    private final UpdateOrderQueryPort updateOrderQueryPort;

    @KafkaListener(
            topics = "${kafka.topics.order-cancelled-topic}",
            groupId = "${kafka.config.consumer.groups.order-cancelled-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderCancelledEventPayload"
    )
    public void onOrderCancelled(@Payload OrderCancelledEventPayload payload,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received OrderCancelledEvent with key: {}", key);
        UUID orderId = UUID.fromString(payload.orderId());
        if (getOrderQueryPort.getOrderById(orderId).isEmpty()) {
            log.warn("Order with id: {} does not exist in the projection, skipping", key);
            return;
        }
        GetOrderResult existing = getOrderQueryPort.getOrderById(orderId).get();
        updateOrderQueryPort.update(new GetOrderResult(
                existing.orderId(), existing.customerId(), existing.totalAmount(),
                "CANCELLED", existing.createdAt(), payload.occurredOn(), existing.items()));
        log.info("Order read model updated to CANCELLED for order: {}", key);
    }

    @DltHandler
    public void onOrderCancelledDlt(@Payload OrderCancelledEventPayload payload,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                    @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process OrderCancelledEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
