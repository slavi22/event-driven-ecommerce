package com.eventdriven.projection.order.adapter.out.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderReadJpaRepository extends JpaRepository<OrderReadEntity, UUID> {
}
