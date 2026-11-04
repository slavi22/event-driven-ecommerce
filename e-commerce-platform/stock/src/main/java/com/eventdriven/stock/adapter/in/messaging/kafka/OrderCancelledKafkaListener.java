package com.eventdriven.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderCancelledEventPayload;
import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.stock.application.command.ReleaseStockCommand;
import com.eventdriven.stock.application.port.in.ReleaseStockUseCase;
import com.eventdriven.stock.application.port.out.idempotency.IsEventProcessedPort;
import com.eventdriven.stock.application.port.out.idempotency.SaveProcessedEventPort;
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
class OrderCancelledKafkaListener {

    private final ReleaseStockUseCase releaseStockUseCase;
    private final IsEventProcessedPort isEventProcessedPort;
    private final SaveProcessedEventPort saveProcessedEventPort;

    @KafkaListener(topics = "${kafka.topics.order-cancelled-topic}",
            groupId = "${kafka.config.consumer.groups.order-cancelled-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderCancelledEventPayload")
    @Transactional
    public void onOrderCancelled(@Payload OrderCancelledEventPayload payload) {
        if (!payload.requiresStockRelease()) {
            return;
        }
        if (isEventProcessedPort.isProcessed(payload.orderId(), "OrderCancelled")) {
            log.warn("Event already processed, skipping");
            return;
        }

        for (OrderItemPayload item : payload.items()) {
            releaseStockUseCase.releaseStock(
                    new ReleaseStockCommand(UUID.fromString(payload.orderId()), UUID.fromString(item.productId()),
                                            item.quantity()));
        }
        saveProcessedEventPort.save(payload.orderId(), "OrderCancelled");
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
