package com.eventdriven.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.stock.application.command.InitializeStockCommand;
import com.eventdriven.stock.application.port.in.InitializeStockUseCase;
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
class ProductCreatedKafkaListener {

    private final InitializeStockUseCase initializeStockUseCase;

    @KafkaListener(
            topics = "${kafka.topics.product-created-topic}",
            groupId = "${kafka.config.consumer.groups.product-created-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductCreatedEventPayload"
    )
    @Transactional
    public void onProductCreated(@Payload ProductCreatedEventPayload payload,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductCreatedEvent with key: {}, in the stock listener", key);
        initializeStockUseCase.initializeStock(
                new InitializeStockCommand(UUID.fromString(payload.productId()), payload.initialQuantity()));
    }

    @DltHandler
    public void onProductCreatedDlt(@Payload ProductCreatedEventPayload payload,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                    @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process ProductCreatedEvent for productId: {}, originalTopic: {}, exception: {} - {}",
                  payload.productId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
