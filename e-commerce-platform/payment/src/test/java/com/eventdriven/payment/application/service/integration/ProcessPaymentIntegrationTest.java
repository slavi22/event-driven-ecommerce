package com.eventdriven.payment.application.service.integration;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.PaymentJpaRepository;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.payment.application.command.ProcessPaymentCommand;
import com.eventdriven.payment.application.port.in.command.ProcessPaymentUseCase;
import com.eventdriven.payment.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.payment.config.PaymentTestConfiguration;
import com.eventdriven.payment.domain.valueobject.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = PaymentTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ProcessPaymentIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("payment");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private ProcessPaymentUseCase processPaymentUseCase;
    @Autowired
    private PaymentJpaRepository paymentJpaRepository;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private SaveOutboxEventPort saveOutboxEventPort;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
        paymentJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a valid command with amount under 10000, when processing payment, then payment row (PROCESSED) and outbox event should be persisted")
    void testProcessPayment_withSuccessfulGateway_shouldPersistPaymentAndOutboxEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        processPaymentUseCase.processPayment(new ProcessPaymentCommand(orderId, new BigDecimal("100.00")));

        // Assert
        var payment = paymentJpaRepository.findByOrderId(orderId);
        assertTrue(payment.isPresent());
        assertEquals(PaymentStatus.PROCESSED, payment.get().getStatus());

        var events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(PaymentProcessedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
        assertEquals(PaymentProcessedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given a valid command with amount over 10000, when processing payment, then payment row (FAILED) and outbox event should be persisted")
    void testProcessPayment_withFailingGateway_shouldPersistPaymentAndFailedOutboxEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        processPaymentUseCase.processPayment(new ProcessPaymentCommand(orderId, new BigDecimal("99999.00")));

        // Assert
        var payment = paymentJpaRepository.findByOrderId(orderId);
        assertTrue(payment.isPresent());
        assertEquals(PaymentStatus.FAILED, payment.get().getStatus());
        assertNotNull(payment.get().getFailureReason());

        var events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertEquals(PaymentFailedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given an outbox write failure, when processing payment, then all writes should be rolled back")
    void testProcessPayment_whenOutboxWriteFails_shouldRollbackAllWrites() {
        // Arrange
        doThrow(new RuntimeException("Outbox write failed")).when(saveOutboxEventPort).save(any());
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> processPaymentUseCase.processPayment(
                        new ProcessPaymentCommand(orderId, new BigDecimal("100.00"))));

        assertTrue(paymentJpaRepository.findByOrderId(orderId).isEmpty());
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }
}
