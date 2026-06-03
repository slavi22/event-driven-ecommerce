package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
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
public class StockReleasedKafkaListener {

    private final OrderSagaOrchestrator orderSagaOrchestrator;

    @KafkaListener(topics = "${kafka.topics.stock-released-topic}",
            groupId = "${kafka.config.consumer.groups.stock-released-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockReleasedEventPayload"
    )
    @Transactional
    public void onStockReleased(@Payload StockReleasedEventPayload payload) {
        log.info("Received StockReleasedEvent for orderId: {}", payload.orderId());
        orderSagaOrchestrator.onStockReleased(payload);
    }

    @DltHandler
    public void onStockReleasedDlt(@Payload StockReleasedEventPayload payload,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                   @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process StockReleasedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }

}
