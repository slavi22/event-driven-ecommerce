package com.eventdriven.projection.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderPlacedEventPayload;
import com.eventdriven.projection.order.application.dto.OrderItemResult;
import com.eventdriven.projection.order.application.port.out.query.GetOrderQueryPort;
import com.eventdriven.projection.order.application.port.out.query.SaveOrderQueryPort;
import com.eventdriven.projection.product.application.dto.GetProductResult;
import com.eventdriven.projection.product.application.port.out.persistence.GetProductQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
class OrderPlacedProjectionListener {

    private final GetOrderQueryPort getOrderQueryPort;
    private final SaveOrderQueryPort saveOrderQueryPort;
    private final OrderEventMapper orderEventMapper;
    private final GetProductQueryPort getProductQueryPort;

    @KafkaListener(
            topics = "${kafka.topics.order-placed-topic}",
            groupId = "${kafka.config.consumer.groups.order-placed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderPlacedEventPayload"
    )
    public void onOrderPlaced(@Payload OrderPlacedEventPayload payload,
                              @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received OrderPlacedEvent with key: {}", key);
        if (getOrderQueryPort.getOrderById(UUID.fromString(key)).isPresent()) {
            log.warn("Order with id: {} already exists in the projection, skipping", key);
            return;
        }
        List<OrderItemResult> items = payload.items().stream()
                .map(i -> {
                    UUID productId = UUID.fromString(i.productId());
                    String productName = getProductQueryPort.getProductById(productId)
                            .map(GetProductResult::name)
                            .orElseGet(() -> {
                                log.warn("Product {} not found in projection, using id as name", productId);
                                return productId.toString();
                            });
                    return new OrderItemResult(productId, productName, i.quantity());
                })
                .toList();
        saveOrderQueryPort.save(orderEventMapper.toGetOrderResult(payload, items));
        log.info("Order read model created for order: {}", key);
    }

    @DltHandler
    public void onOrderPlacedDlt(@Payload OrderPlacedEventPayload payload,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                 @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                 @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process OrderPlacedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
