package com.eventdriven.stock.application.service.integration;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.stock.application.command.InitializeStockCommand;
import com.eventdriven.stock.application.port.in.InitializeStockUseCase;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.config.StockTestConfiguration;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StockTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class InitializeStockIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("command");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private InitializeStockUseCase initializeStockUseCase;
    @Autowired
    private GetStockCommandPort getStockCommandPort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private SaveOutboxEventPort saveOutboxEventPort;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid command, when initializing stock, then should create stock and outbox event")
    void testInitializeStock_withValidCommand_shouldCreateStockAndOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        InitializeStockCommand command = new InitializeStockCommand(productId, 100);

        // Act
        initializeStockUseCase.initializeStock(command);

        // Assert
        assertTrue(getStockCommandPort.getStockByProductId(productId).isPresent());
        List<OutboxEventEntity> events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(StockInitializedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
        assertEquals(StockInitializedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given outbox write fails, when initializing stock, then should rollback both writes")
    void testInitializeStock_whenOutboxWriteFails_shouldRollbackBothWrites() {
        // Arrange
        UUID productId = UUID.randomUUID();
        doThrow(new RuntimeException("Outbox write failed")).when(saveOutboxEventPort).save(any());
        InitializeStockCommand command = new InitializeStockCommand(productId, 100);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> initializeStockUseCase.initializeStock(command));
        assertTrue(getStockCommandPort.getStockByProductId(productId).isEmpty());
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }

    @Test
    @DisplayName("Given stock already exists, when initializing stock again, then should skip and not create duplicate")
    void testInitializeStock_whenStockAlreadyExists_shouldBeIdempotent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        initializeStockUseCase.initializeStock(new InitializeStockCommand(productId, 100));

        // Act
        initializeStockUseCase.initializeStock(new InitializeStockCommand(productId, 200));

        // Assert — original quantity and single outbox event retained
        assertEquals(1, outboxEventJpaRepository.findAll().size());
        assertEquals(100, getStockCommandPort.getStockByProductId(productId)
                .orElseThrow().getQuantity().getValue());
    }
}
