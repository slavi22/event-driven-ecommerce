package com.eventdriven.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.stock.application.command.ReserveStockCommand;
import com.eventdriven.stock.application.port.in.ReserveStockUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class OrderPlacedKafkaListener {

    private final ReserveStockUseCase reserveStockUseCase;

    @KafkaListener(topics = "${kafka.topics.order-placed-topic}",
            groupId = "${kafka.config.consumer.groups.order-placed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderPlacedEventPayload")
    @Transactional
    public void onOrderPlaced(@Payload OrderPlacedEventPayload payload, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received OrderPlacedEvent with key {} and payload {}", key, payload);
        for (OrderItemPayload item : payload.items()) {
            reserveStockUseCase.reserveStock(new ReserveStockCommand(UUID.fromString(payload.orderId()),
                                                                     UUID.fromString(item.productId()),
                                                                     item.quantity()));
        }
    }

    @DltHandler
    public void onOrderPlacedDLT(@Payload OrderPlacedEventPayload payload,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                 @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process OrderPlacedEventPayload for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
