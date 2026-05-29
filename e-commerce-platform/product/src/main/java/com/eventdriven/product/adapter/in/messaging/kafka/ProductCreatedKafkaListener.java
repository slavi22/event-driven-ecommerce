package com.eventdriven.product.adapter.in.messaging.kafka;

import com.eventdriven.product.application.port.out.persistence.query.GetProductQueryPort;
import com.eventdriven.product.application.port.out.persistence.query.SaveProductQueryPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductCreatedEventPayload;
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
            /*
            * I need this otherwise the deserialization of the event payload will fail, since the default type is Object and it cannot be deserialized to ProductCreatedEventPayload without this configuration.
            * I can also set this property in the KafkaConsumerConfiguration, but since this is the only event that I am consuming in this service, I can set it here as well.
            * If I had multiple events that I was consuming, I would set this property in the KafkaConsumerConfiguration and use different consumer groups for each event, so that I can have different deserialization configurations for each event.
            * => https://stackoverflow.com/a/67919991
            * */
            properties = {
                    "spring.json.value.default.type=com.eventdriven.product.domain.event.ProductCreatedEventPayload"}
    )
    public void onProductCreated(@Payload ProductCreatedEventPayload payload,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ProductCreatedEvent with key: {}", key);
        log.info("Going to check if product with id: {} already exists in the projection database", key);
        Optional<Product> existingProduct =
                getProductQueryPort.getProductByProductId(new ProductId(UUID.fromString(key)));
        if (existingProduct.isPresent()) {
            log.warn("Product with id: {} already exists in the projection database, skipping processing of this event",
                     key);
            return;
        }
        log.info("Product with id: {} does not exist in the projection database, processing this event", key);
        saveProductQueryPort.save(productEventMapper.toProductDomainEntity(payload));
    }

    @KafkaListener(
            topics = "${kafka.topics.product-created-topic}.DLT",
            groupId = "${kafka.config.consumer.groups.product-created-events-group}-dlt",
            properties = {
                    "spring.json.value.default.type=com.eventdriven.product.domain.event.ProductCreatedEventPayload"}
    )
    public void onProductCreatedDlt(@Payload ProductCreatedEventPayload payload,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                    @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                                    @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        log.error("Failed to process ProductCreatedEvent for productId: {} | " +
                  "originalTopic: {}, offset: {}, exception: {} - {}",
                  payload.productId(), originalTopic, originalOffset, exceptionClass, exceptionMessage);
        // Here we can maybe implement specific retry logic
    }
}
