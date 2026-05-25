package com.eventdriven.product.application.service;

import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventJpaRepository;
import com.eventdriven.product.adapter.out.persistance.postgres.ProductJpaRepository;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.port.in.CreateProductUseCase;
import com.eventdriven.product.application.port.out.persistence.SaveOutboxEventPort;
import com.eventdriven.product.domain.event.ProductCreatedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class CreateProductIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    @ServiceConnection
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @Autowired
    private CreateProductUseCase createProductUseCase;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private SaveOutboxEventPort saveOutboxEventPort;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid create product request, when create product, then should create product and outbox event")
    void testCreateProduct_withValidRequest_shouldCreateProductAndOutboxEvent() {
        // Arrange
        CreateProductCommand command =
                new CreateProductCommand("Laptop", "A laptop", new BigDecimal("999.99"), ProductCategory.ELECTRONICS);

        // Act
        createProductUseCase.createProduct(command);

        // Assert
        assertEquals(1, productJpaRepository.findAll().size());

        List<OutboxEventEntity> events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(ProductCreatedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
        assertEquals(ProductCreatedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given valid create product request, when create product and exception thrown, then should rollback both writes")
    void testCreateProduct_whenExceptionThrown_shouldRollbackBothWrites() {
        // Arrange
        CreateProductCommand command =
                new CreateProductCommand("Laptop", "A laptop", new BigDecimal("999.99"), ProductCategory.ELECTRONICS);
        doThrow(new RuntimeException("Outbox write failed")).when(saveOutboxEventPort).save(ArgumentMatchers.any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> createProductUseCase.createProduct(command));
        assertEquals(0, productJpaRepository.findAll().size());
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }

}
