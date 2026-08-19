package com.eventdriven.order.application.port.out.productprice;

import com.eventdriven.order.domain.valueobject.Money;

import java.util.Optional;
import java.util.UUID;

public interface GetCachedProductPricePort {

    Optional<Money> getPrice(UUID productId);
}
