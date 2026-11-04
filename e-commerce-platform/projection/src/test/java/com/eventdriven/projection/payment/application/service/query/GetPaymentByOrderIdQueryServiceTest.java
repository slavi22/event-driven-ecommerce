package com.eventdriven.projection.payment.application.service.query;

import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import com.eventdriven.projection.payment.application.exception.PaymentNotFoundException;
import com.eventdriven.projection.payment.application.port.out.query.GetPaymentQueryPort;
import com.eventdriven.projection.payment.application.query.GetPaymentByOrderIdQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPaymentByOrderIdQueryServiceTest {

    @Mock
    private GetPaymentQueryPort getPaymentQueryPort;

    @InjectMocks
    private GetPaymentByOrderIdQueryService getPaymentByOrderIdQueryService;

    @Test
    @DisplayName("Getting a payment for an existing orderId should return the payment result")
    void testGetPaymentByOrderId_withExistingOrderId_shouldReturnResult() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        GetPaymentResult expected = new GetPaymentResult(
                orderId, new BigDecimal("100.00"), "PROCESSED", null, Instant.now(), Instant.now());
        when(getPaymentQueryPort.getPaymentByOrderId(orderId)).thenReturn(Optional.of(expected));

        // Act
        GetPaymentResult result = getPaymentByOrderIdQueryService.getPaymentByOrderId(
                new GetPaymentByOrderIdQuery(orderId));

        // Assert
        assertEquals(expected, result);
        verify(getPaymentQueryPort).getPaymentByOrderId(orderId);
    }

    @Test
    @DisplayName("Getting a payment for a non-existing orderId should throw PaymentNotFoundException")
    void testGetPaymentByOrderId_withNonExistingOrderId_shouldThrowException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(getPaymentQueryPort.getPaymentByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PaymentNotFoundException.class,
                () -> getPaymentByOrderIdQueryService.getPaymentByOrderId(
                        new GetPaymentByOrderIdQuery(orderId)));
        verify(getPaymentQueryPort).getPaymentByOrderId(orderId);
    }
}
