package com.eventdriven.product.adapter.out.persistence.query.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductReadJpaRepository extends JpaRepository<ProductReadEntity, UUID> {
}
