package com.eventdriven.product.application.service.integration;

import com.eventdriven.product.adapter.out.persistence.command.postgres.ProductJpaRepository;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.product.application.command.CreateProductCommand;
import com.eventdriven.product.application.command.DeleteProductCommand;
import com.eventdriven.product.application.port.in.command.CreateProductUseCase;
import com.eventdriven.product.application.port.in.command.DeleteProductUseCase;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.config.ProductTestConfiguration;
import com.eventdriven.product.domain.event.ProductDeletedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductCategory;
import com.eventdriven.product.domain.valueobject.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProductTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class DeleteProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> commandDb =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    static PostgreSQLContainer<?> queryDb =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("product");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.command.jdbc-url", commandDb::getJdbcUrl);
        registry.add("spring.datasource.command.username", commandDb::getUsername);
        registry.add("spring.datasource.command.password", commandDb::getPassword);
        registry.add("spring.datasource.query.jdbc-url", queryDb::getJdbcUrl);
        registry.add("spring.datasource.query.username", queryDb::getUsername);
        registry.add("spring.datasource.query.password", queryDb::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private CreateProductUseCase createProductUseCase;
    @Autowired
    private DeleteProductUseCase deleteProductUseCase;
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
    @DisplayName("Given a valid delete command, when delete product, then should mark product as inactive and save outbox event")
    void testDeleteProduct_withValidCommand_shouldMarkProductAsInactiveAndSaveOutboxEvent() {
        // Arrange
        createProductUseCase.createProduct(
                new CreateProductCommand("Laptop", "A laptop", new BigDecimal("999.99"), ProductCategory.ELECTRONICS, 10));
        outboxEventJpaRepository.deleteAll();

        UUID productId = productJpaRepository.findAll().getFirst().getId();

        // Act
        deleteProductUseCase.deleteProduct(new DeleteProductCommand(productId));

        // Assert
        assertEquals(ProductStatus.INACTIVE, productJpaRepository.findAll().getFirst().getStatus());

        List<OutboxEventEntity> events = outboxEventJpaRepository.findAll();
        assertEquals(1, events.size());
        assertFalse(events.getFirst().isPublished());
        assertEquals(ProductDeletedEventPayload.AGGREGATE_TYPE, events.getFirst().getAggregateType());
        assertEquals(ProductDeletedEventPayload.EVENT_TYPE, events.getFirst().getEventType());
    }

    @Test
    @DisplayName("Given a valid delete command, when exception is thrown, then should rollback both writes")
    void testDeleteProduct_whenExceptionThrown_shouldRollbackBothWrites() {
        // Arrange
        createProductUseCase.createProduct(
                new CreateProductCommand("Laptop", "A laptop", new BigDecimal("999.99"), ProductCategory.ELECTRONICS, 10));
        outboxEventJpaRepository.deleteAll();

        UUID productId = productJpaRepository.findAll().getFirst().getId();
        DeleteProductCommand command = new DeleteProductCommand(productId);
        doThrow(new RuntimeException("Outbox write failed")).when(saveOutboxEventPort).save(ArgumentMatchers.any());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> deleteProductUseCase.deleteProduct(command));
        assertEquals(ProductStatus.ACTIVE, productJpaRepository.findAll().getFirst().getStatus());
        assertEquals(0, outboxEventJpaRepository.findAll().size());
    }
}
