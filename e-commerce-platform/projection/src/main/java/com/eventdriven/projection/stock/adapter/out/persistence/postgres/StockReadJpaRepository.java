package com.eventdriven.projection.stock.adapter.out.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface StockReadJpaRepository extends JpaRepository<StockReadEntity, UUID> {
    Optional<StockReadEntity> findByProductId(UUID productId);
}
