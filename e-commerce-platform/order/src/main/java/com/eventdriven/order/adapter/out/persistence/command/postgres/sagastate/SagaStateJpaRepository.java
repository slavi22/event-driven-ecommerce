package com.eventdriven.order.adapter.out.persistence.command.postgres.sagastate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SagaStateJpaRepository extends JpaRepository<SagaStateEntity, UUID> {
}
