package com.eventdriven.order.application.port.out.productprice;

import com.eventdriven.order.domain.valueobject.Money;

import java.util.UUID;

public interface UpsertProductPricePort {

    void upsert(UUID productId, Money price);
}
