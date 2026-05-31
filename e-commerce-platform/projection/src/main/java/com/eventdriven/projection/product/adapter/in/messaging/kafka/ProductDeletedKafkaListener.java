package com.eventdriven.projection.product.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductDeletedEventPayload;
import com.eventdriven.projection.product.application.port.out.persistence.DeleteProductQueryPort;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
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
class ProductDeletedKafkaListener {

    private final GetProductQueryPort getProductQueryPort;
    private final DeleteProductQueryPort deleteProductQueryPort;

    @KafkaListener(
            topics = "${kafka.topics.product-deleted-topic}",
            groupId = "${kafka.config.consumer.groups.product-deleted-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductDeletedEventPayload"
    )
    void onProductDeleted(@Payload ProductDeletedEventPayload payload,
                          @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductDeletedEvent with key: {}", key);
        if (getProductQueryPort.getProductById(UUID.fromString(key)).isEmpty()) {
            log.warn("Product with id: {} does not exist in the projection, skipping", key);
            return;
        }
        log.info("Marking product with id: {} as inactive in projection", key);
        deleteProductQueryPort.deleteById(UUID.fromString(payload.productId()));
    }

    @KafkaListener(
            topics = "${kafka.topics.product-deleted-topic}.DLT",
            groupId = "${kafka.config.consumer.groups.product-deleted-events-group}-dlt",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductDeletedEventPayload"
    )
    void onProductDeletedDlt(@Payload ProductDeletedEventPayload payload,
                             @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                             @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                             @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                             @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        log.error("Failed to process ProductDeletedEvent for productId: {} | " +
                  "originalTopic: {}, offset: {}, exception: {} - {}",
                  payload.productId(), originalTopic, originalOffset, exceptionClass, exceptionMessage);
    }
}
