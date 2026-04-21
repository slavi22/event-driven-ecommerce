package com.eventdriven.product.adapter.out.messaging;

import com.eventdriven.product.application.port.out.messaging.ProductCreatedEventPublisherPort;
import com.eventdriven.product.domain.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductKafkaAdapter implements ProductCreatedEventPublisherPort {
    // TODO: inject KafkaTemplate and implement the method to publish the message to Kafka topic

    @Override
    public void publishProductCreatedMessage(ProductCreatedEvent event) {

    }

}
