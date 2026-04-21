package com.eventdriven.product.domain.event;

import com.eventdriven.product.domain.entity.Product;
import domain.event.DomainEvent;

import java.time.Instant;

public record ProductCreatedEvent(Product product, Instant occurredOn) implements DomainEvent {
}
