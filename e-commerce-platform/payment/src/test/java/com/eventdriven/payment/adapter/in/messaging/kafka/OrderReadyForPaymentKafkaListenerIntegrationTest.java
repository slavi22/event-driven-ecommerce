package com.eventdriven.payment.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderReadyForPaymentEventPayload;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.PaymentJpaRepository;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.payment.application.port.out.command.SavePaymentPort;
import com.eventdriven.payment.config.PaymentTestConfiguration;
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
        classes = PaymentTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OrderReadyForPaymentKafkaListenerIntegrationTest {

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
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private PaymentJpaRepository paymentJpaRepository;
    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;
    @MockitoSpyBean
    private SavePaymentPort savePaymentPort;

    @Value("${kafka.topics.order-ready-for-payment-topic}")
    private String topic;

    @BeforeEach
    void clean() {
        outboxEventJpaRepository.deleteAll();
        paymentJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Given an OrderReadyForPaymentEvent, when consumed, then a payment row and outbox event should be created")
    void testListener_whenOrderReadyForPaymentReceived_shouldCreatePaymentAndOutboxEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        kafkaTemplate.send(topic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPayload(orderId)));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(paymentJpaRepository.findByOrderId(orderId).isPresent()));

        assertEquals(1, outboxEventJpaRepository.findAll().size());
    }

    @Test
    @DisplayName("Given the same OrderReadyForPaymentEvent received twice, when consumed, then only one payment row should exist")
    void testListener_whenDuplicateEventReceived_shouldProcessOnlyOnce() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String serialized = jsonMapper.writeValueAsString(buildPayload(orderId));

        // Act
        kafkaTemplate.send(topic, orderId.toString(), serialized);
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(paymentJpaRepository.findByOrderId(orderId).isPresent()));

        kafkaTemplate.send(topic, orderId.toString(), serialized);

        // Assert — still only one payment
        await().during(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, paymentJpaRepository.findAll().size()));
    }

    @Test
    @DisplayName("Given the listener encounters an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(savePaymentPort).save(any());
        UUID orderId = UUID.randomUUID();

        // Act
        kafkaTemplate.send(topic, orderId.toString(),
                jsonMapper.writeValueAsString(buildPayload(orderId)));

        // Assert
        try (KafkaConsumer<String, OrderReadyForPaymentEventPayload> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        ConsumerRecords<String, OrderReadyForPaymentEventPayload> records =
                                dltConsumer.poll(Duration.ofMillis(500));
                        assertFalse(records.isEmpty());
                        ConsumerRecord<String, OrderReadyForPaymentEventPayload> consumerRecord = records.iterator().next();
                        assertEquals(orderId.toString(), consumerRecord.key());
                    });
        }
    }

    private OrderReadyForPaymentEventPayload buildPayload(UUID orderId) {
        return new OrderReadyForPaymentEventPayload(
                orderId.toString(), new BigDecimal("100.00"), Instant.now());
    }

    private KafkaConsumer<String, OrderReadyForPaymentEventPayload> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderReadyForPaymentEventPayload.class.getName()
        ));
    }
}
