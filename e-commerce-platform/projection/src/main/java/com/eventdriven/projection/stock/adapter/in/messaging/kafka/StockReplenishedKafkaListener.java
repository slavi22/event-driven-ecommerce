package com.eventdriven.projection.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockReplenishedEventPayload;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.port.out.query.UpdateStockQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class StockReplenishedKafkaListener {

    private final GetStockQueryPort getStockQueryPort;
    private final UpdateStockQueryPort updateStockQueryPort;
    private final StockEventMapper stockEventMapper;

    @KafkaListener(
            topics = "${kafka.topics.stock-replenished-topic}",
            groupId = "${kafka.config.consumer.groups.stock-replenished-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockReplenishedEventPayload"
    )
    public void onStockReplenished(
            @Payload StockReplenishedEventPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received StockReplenishedEvent for product: {}", payload.productId());
        if (getStockQueryPort.getStockByProductId(UUID.fromString(key)).isEmpty()) {
            log.warn("Stock read model for product {} does not exist, skipping", payload.productId());
            return;
        }
        updateStockQueryPort.update(stockEventMapper.toGetStockResult(payload));
        log.info("Stock read model updated for product: {}, new quantity: {}",
                payload.productId(), payload.newQuantity());
    }

    @KafkaListener(
            topics = "${kafka.topics.stock-replenished-topic}.DLT",
            groupId = "${kafka.config.consumer.groups.stock-replenished-events-group}.dlt",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockReplenishedEventPayload"
    )
    public void onStockReplenishedDlt(
            @Payload StockReplenishedEventPayload payload,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMessage,
            @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
            @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
            @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        log.error("DLT: Failed to process StockReplenishedEvent for product: {} | " +
                  "originalTopic: {}, offset: {}, exception: {} - {}",
                payload.productId(), originalTopic, originalOffset, exceptionClass, errorMessage);
    }
}
