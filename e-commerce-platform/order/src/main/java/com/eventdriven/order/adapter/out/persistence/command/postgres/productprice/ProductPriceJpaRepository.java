package com.eventdriven.order.adapter.out.persistence.command.postgres.productprice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProductPriceJpaRepository extends JpaRepository<ProductPriceEntity, UUID> {
}
