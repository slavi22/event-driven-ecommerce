package com.eventdriven.product.application.port.out.messaging;

import com.eventdriven.product.domain.event.ProductCreatedEvent;

public interface ProductCreatedEventPublisherPort {
    void publishProductCreatedMessage(ProductCreatedEvent event);
}
