package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderItemPayload;
import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.projection.config.ProjectionTestConfiguration;
import com.eventdriven.projection.order.adapter.out.persistence.postgres.OrderReadJpaRepository;
import com.eventdriven.projection.order.application.port.out.query.SaveOrderQueryPort;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
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
class OrderPlacedProjectionListenerIntegrationTest {

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
    private OrderReadJpaRepository orderReadJpaRepository;
    @MockitoSpyBean
    private SaveOrderQueryPort saveOrderQueryPort;

    @Value("${kafka.topics.order-placed-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        orderReadJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given an OrderPlacedEvent, when consumed, then the order projection table should be populated")
    void testListener_whenOrderPlacedEventReceived_shouldPopulateProjectionTable() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act & Asseert
        kafkaTemplate.send(topic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPayload(orderId)));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, orderReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given the same OrderPlacedEvent received multiple times, then the projection table should not have duplicate entries")
    void testListener_whenDuplicateOrderPlacedEvent_shouldNotCreateDuplicateEntry() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderPlacedEventPayload payload = buildPayload(orderId);

        // Act & Assert
        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, orderId.toString(), jsonMapper.writeValueAsString(payload));
        }

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, orderReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given multiple distinct OrderPlacedEvents, when consumed, then each order should be saved separately")
    void testListener_whenMultipleDistinctOrderPlacedEvents_shouldSaveEachOrderSeparately() {
        // Arrange
        UUID firstOrderId = UUID.randomUUID();
        UUID secondOrderId = UUID.randomUUID();

        // Act & Assert
        kafkaTemplate.send(topic, firstOrderId.toString(), jsonMapper.writeValueAsString(buildPayload(firstOrderId)));
        kafkaTemplate.send(topic, secondOrderId.toString(), jsonMapper.writeValueAsString(buildPayload(secondOrderId)));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(2, orderReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given the listener encounters an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(saveOrderQueryPort).save(any());
        UUID orderId = UUID.randomUUID();

        // Act & Assert
        kafkaTemplate.send(topic, orderId.toString(), jsonMapper.writeValueAsString(buildPayload(orderId)));

        try (KafkaConsumer<String, OrderPlacedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        ConsumerRecords<String, OrderPlacedEventPayload> records =
                                dltConsumer.poll(Duration.ofMillis(500));
                        assertFalse(records.isEmpty());
                        ConsumerRecord<String, OrderPlacedEventPayload> consumerRecord = records.iterator().next();
                        assertEquals(orderId.toString(), consumerRecord.key());
                    });
        }
    }

    private OrderPlacedEventPayload buildPayload(UUID orderId) {
        return new OrderPlacedEventPayload(
                orderId.toString(),
                UUID.randomUUID().toString(),
                List.of(new OrderItemPayload(UUID.randomUUID().toString(), 2)),
                new BigDecimal("50.00"),
                Instant.now());
    }

    private KafkaConsumer<String, OrderPlacedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderPlacedEventPayload.class.getName()
        ));
    }
}
