package com.eventdriven.order.adapter.out.persistence.command.postgres.productprice;

import com.eventdriven.order.application.port.out.productprice.GetCachedProductPricePort;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProductPriceCachePersistenceAdapter implements GetCachedProductPricePort, UpsertProductPricePort {

    private final ProductPriceJpaRepository productPriceJpaRepository;

    @Override
    public Optional<Money> getPrice(UUID productId) {
        return productPriceJpaRepository.findById(productId)
                .map(e -> Money.of(e.getPrice()));
    }

    @Override
    public void upsert(UUID productId, Money price) {
        ProductPriceEntity entity = productPriceJpaRepository.findById(productId)
                .orElseGet(() -> {
                    ProductPriceEntity newEntity = new ProductPriceEntity();
                    newEntity.setProductId(productId);
                    return newEntity;
                });
        entity.setPrice(price.getAmount());
        entity.setUpdatedAt(Instant.now());
        productPriceJpaRepository.save(entity);
    }
}
