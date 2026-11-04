package com.eventdriven.payment.application.service.command;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.payment.application.command.ProcessPaymentCommand;
import com.eventdriven.payment.application.port.out.command.SavePaymentPort;
import com.eventdriven.payment.application.port.out.external.PaymentGatewayPort;
import com.eventdriven.payment.application.port.out.external.PaymentGatewayResult;
import com.eventdriven.payment.application.port.out.outbox.OutboxEvent;
import com.eventdriven.payment.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.payment.domain.entity.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentServiceTest {

    @Mock
    private SavePaymentPort savePaymentPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ProcessPaymentService processPaymentService;

    @Test
    @DisplayName("Given a successful gateway result, when processing payment, then payment and PaymentProcessed outbox event should be saved")
    void testProcessPayment_whenGatewaySucceeds_shouldSavePaymentAndProcessedOutboxEvent() {
        // Arrange
        when(paymentGatewayPort.charge(any(Payment.class))).thenReturn(new PaymentGatewayResult(true, null));

        // Act
        processPaymentService.processPayment(new ProcessPaymentCommand(UUID.randomUUID(), new BigDecimal("100.00")));

        // Assert
        verify(savePaymentPort, times(1)).save(any(Payment.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(PaymentProcessedEventPayload.class));
        verify(jsonMapper, never()).writeValueAsString(any(PaymentFailedEventPayload.class));
    }

    @Test
    @DisplayName("Given a failed gateway result, when processing payment, then payment and PaymentFailed outbox event should be saved")
    void testProcessPayment_whenGatewayFails_shouldSavePaymentAndFailedOutboxEvent() {
        // Arrange
        when(paymentGatewayPort.charge(any(Payment.class)))
                .thenReturn(new PaymentGatewayResult(false, "Insufficient funds"));

        // Act
        processPaymentService.processPayment(new ProcessPaymentCommand(UUID.randomUUID(), new BigDecimal("99999.00")));

        // Assert
        verify(savePaymentPort, times(1)).save(any(Payment.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
        verify(jsonMapper, times(1)).writeValueAsString(any(PaymentFailedEventPayload.class));
        verify(jsonMapper, never()).writeValueAsString(any(PaymentProcessedEventPayload.class));
    }
}
