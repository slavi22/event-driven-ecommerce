package com.eventdriven.projection.payment.adapter.in.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.projection.config.ProjectionTestConfiguration;
import com.eventdriven.projection.payment.adapter.out.persistence.postgres.PaymentReadJpaRepository;
import com.eventdriven.projection.payment.application.port.out.query.SavePaymentQueryPort;
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
class PaymentFailedProjectionListenerIntegrationTest {

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
    private PaymentReadJpaRepository paymentReadJpaRepository;
    @MockitoSpyBean
    private SavePaymentQueryPort savePaymentQueryPort;

    @Value("${kafka.topics.payment-failed-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        paymentReadJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given a PaymentFailedEvent, when consumed, then the payment projection should be created with FAILED status and failure reason")
    void testListener_whenPaymentFailedEventReceived_shouldCreateProjection() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        kafkaTemplate.send(topic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPayload(orderId)));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertEquals(1, paymentReadJpaRepository.findAll().size());
                    var entity = paymentReadJpaRepository.findById(orderId).orElseThrow();
                    assertEquals("FAILED", entity.getStatus());
                    assertEquals("Insufficient funds", entity.getFailureReason());
                    assertNull(entity.getAmount());
                });
    }

    @Test
    @DisplayName("Given the same PaymentFailedEvent received multiple times, then only one projection row should exist")
    void testListener_whenDuplicatePaymentFailedEvent_shouldNotCreateDuplicateEntry() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        PaymentFailedEventPayload payload = buildPayload(orderId);

        // Act
        for (int i = 0; i < 3; i++) {
            kafkaTemplate.send(topic, orderId.toString(), jsonMapper.writeValueAsString(payload));
        }

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, paymentReadJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given the listener encounters an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(savePaymentQueryPort).save(any());
        UUID orderId = UUID.randomUUID();

        // Act
        kafkaTemplate.send(topic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPayload(orderId)));

        // Assert
        try (KafkaConsumer<String, PaymentFailedEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        ConsumerRecords<String, PaymentFailedEventPayload> records =
                                dltConsumer.poll(Duration.ofMillis(500));
                        assertFalse(records.isEmpty());
                        ConsumerRecord<String, PaymentFailedEventPayload> record = records.iterator().next();
                        assertEquals(orderId.toString(), record.key());
                    });
        }
    }

    private PaymentFailedEventPayload buildPayload(UUID orderId) {
        return new PaymentFailedEventPayload(
                orderId.toString(), "Insufficient funds", Instant.now());
    }

    private KafkaConsumer<String, PaymentFailedEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentFailedEventPayload.class.getName()
        ));
    }
}
