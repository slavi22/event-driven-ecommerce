package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.order.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import com.eventdriven.order.application.command.OrderItemCommand;
import com.eventdriven.order.application.command.PlaceOrderCommand;
import com.eventdriven.order.application.dto.PlaceOrderResult;
import com.eventdriven.order.application.port.in.command.PlaceOrderUseCase;
import com.eventdriven.order.application.port.out.command.GetOrderPort;
import com.eventdriven.order.application.port.out.productprice.UpsertProductPricePort;
import com.eventdriven.order.application.port.out.sagastate.GetSagaStatePort;
import com.eventdriven.order.application.port.out.sagastate.SagaStep;
import com.eventdriven.order.application.service.command.OrderSagaOrchestrator;
import com.eventdriven.order.config.OrderTestConfiguration;
import com.eventdriven.order.domain.valueobject.Money;
import com.eventdriven.order.domain.valueobject.OrderStatus;
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
        classes = OrderTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class StockReservedKafkaListenerIntegrationTest {

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
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private JsonMapper jsonMapper;
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
    private OrderSagaOrchestrator orderSagaOrchestrator;

    @Value("${kafka.topics.stock-reserved-topic}")
    private String stockReservedTopic;
    @Value("${kafka.topics.stock-reservation-failed-topic}")
    private String stockReservationFailedTopic;

    private UUID productId;

    @BeforeEach
    void setUp() {
        outboxEventJpaRepository.deleteAll();
        productId = UUID.randomUUID();
        upsertProductPricePort.upsert(productId, Money.of(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("Given a StockReservedEvent, when consumed, then order should advance to STOCK_RESERVED and saga to PAYMENT")
    void testListener_whenStockReservedEventReceived_shouldAdvanceSaga() {
        // Arrange
        PlaceOrderResult placed = placeOrder();

        // Act & Assert
        kafkaTemplate.send(stockReservedTopic, placed.orderId().toString(),
                jsonMapper.writeValueAsString(stockReservedPayload(placed.orderId())));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(OrderStatus.STOCK_RESERVED,
                                getOrderPort.getOrder(placed.orderId()).getStatus()));
        assertEquals(SagaStep.PAYMENT, getSagaStatePort.getSagaState(placed.orderId()).currentStep());
    }

    @Test
    @DisplayName("Given a StockReservationFailedEvent, when consumed, then order should be CANCELLED")
    void testListener_whenStockReservationFailedEventReceived_shouldCancelOrder() {
        // Arrange
        PlaceOrderResult placed = placeOrder();

        StockReservationFailedEventPayload payload = new StockReservationFailedEventPayload(
                placed.orderId().toString(), productId.toString(), "Out of stock", Instant.now());

        // Act & Assert
        kafkaTemplate.send(stockReservationFailedTopic, placed.orderId().toString(),
                jsonMapper.writeValueAsString(payload));

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertEquals(OrderStatus.CANCELLED,
                                getOrderPort.getOrder(placed.orderId()).getStatus()));
    }

    @Test
    @DisplayName("Given the listener throws an exception, when consuming StockReservedEvent, then message should be sent to DLT")
    void testListener_whenExceptionThrown_shouldSendMessageToDlt() {
        // Arrange
        doThrow(new RuntimeException("Simulated failure"))
                .when(orderSagaOrchestrator).onStockReserved(any());
        PlaceOrderResult placed = placeOrder();

        // Act
        kafkaTemplate.send(stockReservedTopic, placed.orderId().toString(),
                jsonMapper.writeValueAsString(stockReservedPayload(placed.orderId())));

        // Assert
        try (KafkaConsumer<String, StockReservedEventPayload> dltConsumer = buildDltConsumer(StockReservedEventPayload.class)) {
            dltConsumer.subscribe(List.of(stockReservedTopic + DLT.getValue()));
            await().atMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        ConsumerRecords<String, StockReservedEventPayload> records =
                                dltConsumer.poll(Duration.ofMillis(500));
                        assertFalse(records.isEmpty());
                        ConsumerRecord<String, StockReservedEventPayload> record = records.iterator().next();
                        assertEquals(placed.orderId().toString(), record.key());
                    });
        }
    }

    private PlaceOrderResult placeOrder() {
        return placeOrderUseCase.placeOrder(
                new PlaceOrderCommand(UUID.randomUUID(), List.of(new OrderItemCommand(productId, 1))));
    }

    private StockReservedEventPayload stockReservedPayload(UUID orderId) {
        return new StockReservedEventPayload(
                UUID.randomUUID().toString(), productId.toString(),
                orderId.toString(), 1, 99, Instant.now());
    }

    private <T> KafkaConsumer<String, T> buildDltConsumer(Class<T> valueType) {
        return new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-dlt-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class.getName(),
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName()
        ));
    }
}
