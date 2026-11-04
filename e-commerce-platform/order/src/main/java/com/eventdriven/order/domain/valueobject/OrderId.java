package com.eventdriven.order.domain.valueobject;

import domain.valueobject.BaseId;

import java.util.UUID;

public class OrderId extends BaseId<UUID> {

    public OrderId(UUID value) {
        super(value);
    }
}
