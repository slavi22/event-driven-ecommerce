package com.eventdriven.payment.domain.entity;

import com.eventdriven.payment.domain.exception.PaymentDomainException;
import com.eventdriven.payment.domain.valueobject.Money;
import com.eventdriven.payment.domain.valueobject.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDomainEntityTest {

    @Test
    @DisplayName("Payment.initiate should create a PENDING payment with a generated ID")
    void testInitiate_shouldCreatePendingPaymentWithId() {
        Payment payment = buildPendingPayment();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getId());
        assertNotNull(payment.getId().getValue());
    }

    @Test
    @DisplayName("Payment.initiate with null orderId should throw PaymentDomainException")
    void testInitiate_withNullOrderId_shouldThrowException() {
        assertThrows(PaymentDomainException.class,
                () -> Payment.initiate(null, Money.of(new BigDecimal("100.00"))));
    }

    @Test
    @DisplayName("Payment.initiate with null amount should throw PaymentDomainException")
    void testInitiate_withNullAmount_shouldThrowException() {
        assertThrows(PaymentDomainException.class,
                () -> Payment.initiate(UUID.randomUUID(), null));
    }

    @Test
    @DisplayName("markProcessed on PENDING payment should transition to PROCESSED")
    void testMarkProcessed_fromPending_shouldTransitionToProcessed() {
        Payment payment = buildPendingPayment();

        payment.markProcessed();

        assertEquals(PaymentStatus.PROCESSED, payment.getStatus());
        assertNull(payment.getFailureReason());
    }

    @Test
    @DisplayName("markProcessed on non-PENDING payment should throw PaymentDomainException")
    void testMarkProcessed_fromNonPending_shouldThrowException() {
        Payment payment = buildPendingPayment();
        payment.markProcessed();

        assertThrows(PaymentDomainException.class, payment::markProcessed);
    }

    @Test
    @DisplayName("markFailed on PENDING payment should transition to FAILED and set reason")
    void testMarkFailed_fromPending_shouldTransitionToFailedWithReason() {
        Payment payment = buildPendingPayment();

        payment.markFailed("Insufficient funds");

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("Insufficient funds", payment.getFailureReason());
    }

    @Test
    @DisplayName("markFailed on non-PENDING payment should throw PaymentDomainException")
    void testMarkFailed_fromNonPending_shouldThrowException() {
        Payment payment = buildPendingPayment();
        payment.markFailed("Insufficient funds");

        assertThrows(PaymentDomainException.class, () -> payment.markFailed("Another reason"));
    }

    @Test
    @DisplayName("markProcessed on FAILED payment should throw PaymentDomainException")
    void testMarkProcessed_fromFailed_shouldThrowException() {
        Payment payment = buildPendingPayment();
        payment.markFailed("Insufficient funds");

        assertThrows(PaymentDomainException.class, payment::markProcessed);
    }

    private Payment buildPendingPayment() {
        return Payment.initiate(UUID.randomUUID(), Money.of(new BigDecimal("100.00")));
    }
}
