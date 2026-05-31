package com.eventdriven.projection.product.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductUpdatedEventPayload;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
import com.eventdriven.projection.product.application.port.out.persistence.UpdateProductQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class ProductUpdatedKafkaListener {

    private final GetProductQueryPort getProductQueryPort;
    private final UpdateProductQueryPort updateProductQueryPort;
    private final ProductEventMapper productEventMapper;

    @KafkaListener(
            topics = "${kafka.topics.product-updated-topic}",
            groupId = "${kafka.config.consumer.groups.product-updated-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductUpdatedEventPayload"
    )
    void onProductUpdated(@Payload ProductUpdatedEventPayload payload,
                          @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductUpdatedEvent with key: {}", key);
        if (getProductQueryPort.getProductById(UUID.fromString(key)).isEmpty()) {
            log.warn("Product with id: {} does not exist in the projection, skipping", key);
            return;
        }
        log.info("Updating product with id: {} in projection", key);
        updateProductQueryPort.update(productEventMapper.toGetProductResult(payload));
    }

    @KafkaListener(
            topics = "${kafka.topics.product-updated-topic}.DLT",
            groupId = "${kafka.config.consumer.groups.product-updated-events-group}-dlt",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductUpdatedEventPayload"
    )
    void onProductUpdatedDlt(@Payload ProductUpdatedEventPayload payload,
                             @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                             @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                             @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                             @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        log.error("Failed to process ProductUpdatedEvent for productId: {} | " +
                  "originalTopic: {}, offset: {}, exception: {} - {}",
                  payload.productId(), originalTopic, originalOffset, exceptionClass, exceptionMessage);
    }
}
