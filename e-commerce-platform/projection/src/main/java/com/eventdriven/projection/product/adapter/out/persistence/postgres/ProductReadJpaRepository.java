package com.eventdriven.projection.product.adapter.out.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductReadJpaRepository extends JpaRepository<ProductReadEntity, UUID> {
}
