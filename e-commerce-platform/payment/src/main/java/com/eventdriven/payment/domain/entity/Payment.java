package com.eventdriven.payment.domain.entity;

import com.eventdriven.payment.domain.exception.PaymentDomainException;
import com.eventdriven.payment.domain.valueobject.Money;
import com.eventdriven.payment.domain.valueobject.PaymentId;
import com.eventdriven.payment.domain.valueobject.PaymentStatus;
import domain.entity.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class Payment extends AggregateRoot<PaymentId> {

    private UUID orderId;
    private Money amount;
    private PaymentStatus status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static Payment initiate(UUID orderId, Money amount) {
        Payment payment = new Payment();
        payment.setId(new PaymentId(UUID.randomUUID()));
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        payment.createdAt = Instant.now();
        payment.updatedAt = Instant.now();
        payment.validate();
        return payment;
    }

    public static Payment reconstitute(PaymentId id, UUID orderId, Money amount,
                                       PaymentStatus status, String failureReason,
                                       Instant createdAt, Instant updatedAt) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = status;
        payment.failureReason = failureReason;
        payment.createdAt = createdAt;
        payment.updatedAt = updatedAt;
        return payment;
    }

    public void markProcessed() {
        if (status != PaymentStatus.PENDING) {
            throw new PaymentDomainException("Cannot mark processed: payment is not PENDING");
        }
        this.status = PaymentStatus.PROCESSED;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        if (status != PaymentStatus.PENDING) {
            throw new PaymentDomainException("Cannot mark failed: payment is not PENDING");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    private void validate() {
        if (orderId == null) {
            throw new PaymentDomainException("Order ID cannot be null");
        }
        if (amount == null) {
            throw new PaymentDomainException("Amount cannot be null");
        }
    }

    private Payment() {
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
