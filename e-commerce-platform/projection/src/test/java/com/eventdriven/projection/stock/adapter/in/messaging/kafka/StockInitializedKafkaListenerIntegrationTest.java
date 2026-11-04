package com.eventdriven.projection.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.projection.config.ProjectionTestConfiguration;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.port.out.query.SaveStockQueryPort;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static adapter.Constants.DLT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ProjectionTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class StockInitializedKafkaListenerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> db =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.3")).withDatabaseName("projection");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private GetStockQueryPort getStockQueryPort;
    @MockitoSpyBean
    private SaveStockQueryPort saveStockQueryPort;

    @Value("${kafka.topics.stock-initialized-topic}")
    private String topic;

    @Test
    @DisplayName("Given a StockInitializedEvent, when consumed, then the stock read model should be created")
    void testListener_whenStockInitializedEventReceived_shouldCreateReadModel() {
        // Arrange
        UUID productId = UUID.randomUUID();
        StockInitializedEventPayload payload = buildPayload(productId, 100);

        // Act
        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   assertTrue(getStockQueryPort.getStockByProductId(productId).isPresent());
                   assertEquals(100, getStockQueryPort.getStockByProductId(productId).orElseThrow().quantity());
               });
    }

    @Test
    @DisplayName("Given the same StockInitializedEvent received multiple times, then only one read model entry should exist")
    void testListener_whenDuplicateStockInitializedEvent_shouldBeIdempotent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        StockInitializedEventPayload payload = buildPayload(productId, 50);

        // Act
        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));
        }

        // Assert — entry exists and quantity is unchanged after duplicate processing
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> assertTrue(
                       getStockQueryPort.getStockByProductId(productId).isPresent()));
        await().pollDelay(2, TimeUnit.SECONDS)
               .atMost(5, TimeUnit.SECONDS)
               .untilAsserted(() -> assertEquals(50,
                       getStockQueryPort.getStockByProductId(productId).orElseThrow().quantity()));
    }

    @Test
    @DisplayName("Given the listener throws an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(saveStockQueryPort).save(any());
        UUID productId = UUID.randomUUID();
        StockInitializedEventPayload payload = buildPayload(productId, 10);

        // Act
        kafkaTemplate.send(topic, productId.toString(), jsonMapper.writeValueAsString(payload));

        // Assert
        try (KafkaConsumer<String, StockInitializedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                   .untilAsserted(() -> {
                       ConsumerRecords<String, StockInitializedEventPayload> records =
                               dltConsumer.poll(Duration.ofMillis(500));
                       assertFalse(records.isEmpty());
                       ConsumerRecord<String, StockInitializedEventPayload> record = records.iterator().next();
                       assertEquals(productId.toString(), record.key());
                   });
        }
    }

    private StockInitializedEventPayload buildPayload(UUID productId, int quantity) {
        return new StockInitializedEventPayload(
                UUID.randomUUID().toString(), productId.toString(), quantity, Instant.now()
        );
    }

    private KafkaConsumer<String, StockInitializedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, StockInitializedEventPayload.class.getName()
        ));
    }
}
