package com.eventdriven.stock.domain.valueobject;

import domain.valueobject.BaseId;

import java.util.UUID;

public class StockId extends BaseId<UUID> {
    public StockId(UUID value) {
        super(value);
    }
}
