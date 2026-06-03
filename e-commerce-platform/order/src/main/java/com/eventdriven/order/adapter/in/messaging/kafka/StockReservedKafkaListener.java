package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.order.application.service.command.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
class StockReservedKafkaListener {

    private final OrderSagaOrchestrator orderSagaOrchestrator;

    @KafkaListener(topics = "${kafka.topics.stock-reserved-topic}",
            groupId = "${kafka.config.consumer.groups.stock-reserved-events-group}",
            properties = {
                    "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockReservedEventPayload"})
    @Transactional
    public void onStockReservedEvent(@Payload StockReservedEventPayload payload) {
        log.info("Received StockReservedEvent for order id: {}", payload.orderId());
        orderSagaOrchestrator.onStockReserved(payload);
    }

    @DltHandler
    public void onStockReservedDlt(@Payload StockReservedEventPayload payload,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                   @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process StockReservedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }

    @KafkaListener(topics = "${kafka.topics.stock-reservation-failed-topic}",
            groupId = "${kafka.config.consumer.groups.stock-reservation-failed-events-group}",
            properties = {
                    "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload"})
    @Transactional
    public void onStockReservationFailedEvent(@Payload StockReservationFailedEventPayload payload) {
        log.info("Received StockReservationFailedEvent for order id: {}", payload.orderId());
        orderSagaOrchestrator.onStockReservationFailed(payload);
    }

    @DltHandler
    public void onStockReservationFailedDlt(@Payload StockReservationFailedEventPayload payload,
                                            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                            @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                            @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process StockReservationFailedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
