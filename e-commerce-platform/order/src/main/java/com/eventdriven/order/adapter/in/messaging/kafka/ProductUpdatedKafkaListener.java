package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.product.event.ProductUpdatedEventPayload;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.domain.valueobject.Money;
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
class ProductUpdatedKafkaListener {

    private final UpsertProductPricePort upsertProductPricePort;

    @KafkaListener(topics = "${kafka.topics.product-updated-topic}",
            groupId = "${kafka.config.consumer.groups.product-updated-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.product.event.ProductUpdatedEventPayload")
    @Transactional
    public void onProductUpdated(@Payload ProductUpdatedEventPayload payload) {
        log.info("Updating cached price for product {}: {}", payload.productId(), payload.price());
        upsertProductPricePort.upsert(UUID.fromString(payload.productId()), Money.of(payload.price()));
    }

    @DltHandler
    public void onProductUpdatedDlt(@Payload ProductUpdatedEventPayload payload,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                    @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                    @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to update cached price for product: {}, originalTopic: {}, exception: {} - {}",
                payload.productId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
