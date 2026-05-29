package com.eventdriven.product.adapter.in.messaging.kafka;

import com.eventdriven.product.application.port.out.persistence.query.GetProductQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.UpdateProductQueryPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductUpdatedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProductUpdatedKafkaListener {

    private final GetProductQueryPort getProductQueryPort;
    private final UpdateProductQueryPort updateProductQueryPort;
    private final ProductEventMapper productEventMapper;

    @KafkaListener(topics = "${kafka.topics.product-updated-topic}",
            groupId = "${kafka.config.consumer.groups.product-updated-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.product.domain.event.ProductUpdatedEventPayload")
    void onProductUpdated(@Payload ProductUpdatedEventPayload payload, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductUpdatedEvent with key: {}", key);
        log.info("Going to check if product with id: {} already exists in the projection database", key);
        Optional<Product> product = getProductQueryPort.getProductByProductId(new ProductId(UUID.fromString(key)));

        if (product.isEmpty()) {
            log.warn("Product with id: {} does not exist in the projection database, skipping processing of this event",
                     key);
            return;
        }
        log.info("Product with id: {} exists in the projection database, processing this event", key);
        updateProductQueryPort.update(productEventMapper.toProductDomainEntity(payload));
    }

    @KafkaListener(topics = "${kafka.topics.product-updated-topic}.DLT",
            groupId = "${kafka.config.consumer.groups.product-updated-events-group}-dlt",
            properties = "spring.json.value.default.type=com.eventdriven.product.domain.event.ProductUpdatedEventPayload")
    void onProductUpdatedDlt(@Payload ProductUpdatedEventPayload payload,
                             @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                             @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                             @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                             @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        log.error("Failed to process ProductUpdatedEvent for productId: {} | " +
                  "originalTopic: {}, offset: {}, exception: {} - {}",
                  payload.productId(), originalTopic, originalOffset, exceptionClass, exceptionMessage);
        // Implement specific retry logic
    }
}
