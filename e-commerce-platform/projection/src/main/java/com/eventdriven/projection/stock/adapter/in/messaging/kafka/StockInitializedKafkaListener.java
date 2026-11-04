package com.eventdriven.projection.stock.adapter.in.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.port.out.query.SaveStockQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public class StockInitializedKafkaListener {

    private final GetStockQueryPort getStockQueryPort;
    private final SaveStockQueryPort saveStockQueryPort;
    private final StockEventMapper stockEventMapper;

    @KafkaListener(
            topics = "${kafka.topics.stock-initialized-topic}",
            groupId = "${kafka.config.consumer.groups.stock-initialized-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.stock.event.StockInitializedEventPayload"
    )
    public void onStockInitialized(
            @Payload StockInitializedEventPayload payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received StockInitializedEvent for product: {}", payload.productId());
        if (getStockQueryPort.getStockByProductId(UUID.fromString(key)).isPresent()) {
            log.warn("Stock read model for product {} already exists, skipping", payload.productId());
            return;
        }
        saveStockQueryPort.save(stockEventMapper.toGetStockResult(payload));
        log.info("Stock read model created for product: {}", payload.productId());
    }

    @DltHandler
    public void onStockInitializedDlt(
            @Payload StockInitializedEventPayload payload,
            @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMessage,
            @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
            @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("DLT: Failed to process StockInitializedEvent for product: {}, originalTopic: {}, exception: {} - {}",
                payload.productId(), originalTopic, exceptionClass, errorMessage);
    }
}
