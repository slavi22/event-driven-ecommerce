package com.eventdriven.projection.product.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductCreatedEventPayload;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
import com.eventdriven.projection.product.application.port.out.persistence.SaveProductQueryPort;
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
class ProductCreatedKafkaListener {

    private final GetProductQueryPort getProductQueryPort;
    private final SaveProductQueryPort saveProductQueryPort;
    private final ProductEventMapper productEventMapper;

    @KafkaListener(
            topics = "${kafka.topics.product-created-topic}",
            groupId = "${kafka.config.consumer.groups.product-created-events-group}",
            properties = {
                    "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductCreatedEventPayload"
            }
    )
    public void onProductCreated(@Payload ProductCreatedEventPayload payload,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductCreatedEvent with key: {}", key);
        if (getProductQueryPort.getProductById(UUID.fromString(key)).isPresent()) {
            log.warn("Product with id: {} already exists in the projection, skipping", key);
            return;
        }
        log.info("Saving product with id: {} to projection", key);
        saveProductQueryPort.save(productEventMapper.toGetProductResult(payload));
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
