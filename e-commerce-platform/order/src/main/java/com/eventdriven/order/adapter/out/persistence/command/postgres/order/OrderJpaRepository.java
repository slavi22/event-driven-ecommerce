package com.eventdriven.order.adapter.out.persistence.command.postgres.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    // we need this because otherwise we will get LazyInitializationException when we try to access the items of the order outside the transaction
    // can be observed if we use the default findById in the StockReleasedKafkaListenerIntegrationTest
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);
}
