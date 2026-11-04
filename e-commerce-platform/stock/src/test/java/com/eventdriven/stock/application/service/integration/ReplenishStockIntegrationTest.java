package com.eventdriven.stock.application.service.integration;

import com.eventdriven.contracts.stock.event.StockReplenishedEventPayload;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.stock.application.command.InitializeStockCommand;
import com.eventdriven.stock.application.command.ReplenishStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.port.in.InitializeStockUseCase;
import com.eventdriven.stock.application.port.in.ReplenishStockUseCase;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StockTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class ReplenishStockIntegrationTest {

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
    private ReplenishStockUseCase replenishStockUseCase;
    @Autowired
    private GetStockCommandPort getStockCommandPort;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given stock exists, when replenishing, then should update quantity and create outbox event")
    void testReplenishStock_withExistingStock_shouldUpdateQuantityAndCreateOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        initializeStockUseCase.initializeStock(new InitializeStockCommand(productId, 50));
        outboxEventJpaRepository.deleteAll();

        // Act
        replenishStockUseCase.replenishStock(new ReplenishStockCommand(productId, 30));

        // Assert
        int newQuantity = getStockCommandPort.getStockByProductId(productId)
                .orElseThrow().getQuantity().getValue();
        assertEquals(80, newQuantity);
        List<OutboxEventEntity> events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(StockReplenishedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
        assertEquals(StockReplenishedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
    }

    @Test
    @DisplayName("Given stock does not exist, when replenishing, then should throw StockNotFoundException")
    void testReplenishStock_whenStockNotFound_shouldThrowStockNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();

        // Act & Assert
        assertThrows(StockNotFoundException.class,
                () -> replenishStockUseCase.replenishStock(new ReplenishStockCommand(productId, 10)));
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }
}
