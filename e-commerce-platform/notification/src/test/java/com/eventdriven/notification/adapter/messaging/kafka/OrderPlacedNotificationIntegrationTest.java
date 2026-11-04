package com.eventdriven.notification.adapter.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.notification.application.port.out.NotificationPort;
import com.eventdriven.notification.application.port.out.model.Notification;
import com.eventdriven.notification.config.NotificationTestConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static adapter.Constants.DLT;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = NotificationTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class OrderPlacedNotificationIntegrationTest {

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
    @MockitoSpyBean
    private NotificationPort notificationPort;

    @Value("${kafka.topics.order-placed-topic}")
    private String topic;

    @Test
    @DisplayName("Given an OrderPlacedEventPayload, when consumed, then a notification should be sent with the correct recipient and order details")
    void testListener_whenOrderPlacedReceived_shouldSendNotification() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal totalAmount = new BigDecimal("199.99");

        // Act
        kafkaTemplate.send(topic, orderId.toString(), jsonMapper.writeValueAsString(
                buildPayload(orderId, customerId, totalAmount)));

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(notificationPort, times(1)).send(new Notification(
                        customerId.toString(),
                        "Order received",
                        "Your order %s has been received. Total: %s".formatted(orderId, totalAmount)
                )));
    }

    @Test
    @DisplayName("Given the listener encounters an exception, then the message should be sent to the DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure")).when(notificationPort).send(any());
        UUID orderId = UUID.randomUUID();

        // Act
        kafkaTemplate.send(topic, orderId.toString(), jsonMapper.writeValueAsString(
                buildPayload(orderId, UUID.randomUUID(), new BigDecimal("99.00"))));

        // Assert
        try (KafkaConsumer<String, String> dltConsumer = buildDltConsumer()) {
            dltConsumer.subscribe(List.of(topic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        ConsumerRecords<String, String> records = dltConsumer.poll(Duration.ofMillis(500));
                        assertFalse(records.isEmpty());
                    });
        }
    }

    private OrderPlacedEventPayload buildPayload(UUID orderId, UUID customerId, BigDecimal totalAmount) {
        return new OrderPlacedEventPayload(
                orderId.toString(),
                customerId.toString(),
                List.of(),
                totalAmount,
                Instant.now());
    }

    private KafkaConsumer<String, String> buildDltConsumer() {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()
        ));
    }
}
