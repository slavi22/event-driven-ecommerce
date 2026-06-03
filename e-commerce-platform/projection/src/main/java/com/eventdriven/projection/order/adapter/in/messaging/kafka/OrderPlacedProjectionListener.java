package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.port.out.query.SaveOrderQueryPort;
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
class OrderPlacedProjectionListener {

    private final GetOrderQueryPort getOrderQueryPort;
    private final SaveOrderQueryPort saveOrderQueryPort;
    private final OrderEventMapper orderEventMapper;

    @KafkaListener(
            topics = "${kafka.topics.order-placed-topic}",
            groupId = "${kafka.config.consumer.groups.order-placed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderPlacedEventPayload"
    )
    public void onOrderPlaced(@Payload OrderPlacedEventPayload payload,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received OrderPlacedEvent with key: {}", key);
        if (getOrderQueryPort.getOrderById(UUID.fromString(key)).isPresent()) {
            log.warn("Order with id: {} already exists in the projection, skipping", key);
            return;
        }
        saveOrderQueryPort.save(orderEventMapper.toGetOrderResult(payload));
        log.info("Order read model created for order: {}", key);
    }

    @DltHandler
    public void onOrderPlacedDlt(@Payload OrderPlacedEventPayload payload,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                 @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process OrderPlacedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
