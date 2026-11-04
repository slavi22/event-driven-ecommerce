package com.eventdriven.order.application.service.integration;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.order.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.order.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.order.application.command.OrderItemCommand;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.application.port.out.sagastate.GetSagaStatePort;
import com.eventdriven.order.application.port.out.sagastate.SagaStatus;
import com.eventdriven.order.application.port.out.sagastate.SagaStep;
import com.eventdriven.order.config.OrderTestConfiguration;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderStatus;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = OrderTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class PlaceOrderIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("order");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;
    @Autowired
    private GetOrderPort getOrderPort;
    @Autowired
    private GetSagaStatePort getSagaStatePort;
    @Autowired
    private UpsertProductPricePort upsertProductPricePort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private SaveOutboxEventPort saveOutboxEventPort;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a valid command, when placing an order, then order, saga state and outbox event should be persisted")
    void testPlaceOrder_withValidCommand_shouldPersistOrderSagaStateAndOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("25.00")));

        //Act & Assert
        var result = placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(customerId, List.of(new OrderItemCommand(productId, 2))));

        assertNotNull(result.orderId());

        var order = getOrderPort.getOrder(result.orderId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(customerId, order.getCustomerId());

        var saga = getSagaStatePort.getSagaState(result.orderId());
        assertEquals(SagaStep.STOCK_RESERVATION, saga.currentStep());
        assertEquals(SagaStatus.STARTED, saga.status());

        List<OutboxEventEntity> events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(OrderPlacedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
        assertEquals(OrderPlacedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given an outbox write failure, when placing an order, then all writes should be rolled back")
    void testPlaceOrder_whenOutboxWriteFails_shouldRollbackAllWrites() {
        // Arrange
        UUID productId = UUID.randomUUID();
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("10.00")));
        doThrow(new RuntimeException("Outbox write failed")).when(saveOutboxEventPort).save(any());
        var command = new PlaceOrderCommand(UUID.randomUUID(), List.of(new OrderItemCommand(productId, 1)));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> placeOrderUseCase.placeOrder(command));
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }
}
