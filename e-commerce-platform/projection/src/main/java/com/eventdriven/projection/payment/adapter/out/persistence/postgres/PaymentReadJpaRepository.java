package com.eventdriven.projection.payment.adapter.out.persistence.postgres;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentReadJpaRepository extends JpaRepository<PaymentReadEntity, UUID> {
}
