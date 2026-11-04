package com.eventdriven.product.domain.valueobject;

import domain.valueobject.BaseId;

import java.util.UUID;

public class ProductId extends BaseId<UUID> {
    public ProductId(UUID value) {
        super(value);
    }
}
