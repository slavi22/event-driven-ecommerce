package com.eventdriven.projection.payment.adapter.out.persistence.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_read", schema = "payments")
public class PaymentReadEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID orderId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;
}
