package com.eventdriven.stock.adapter.out.messaging.kafka;

import com.eventdriven.contracts.stock.event.StockDepletedEventPayload;
import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.contracts.stock.event.StockReplenishedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.stock.adapter.out.messaging.kafka.config.KafkaTopicProperties;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.stock.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class OutboxEventPoller {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Scheduled(fixedDelayString = "${app.scheduling.stock}")
    @Transactional
    public void readOutbox() {
        List<OutboxEventEntity> unpublishedEvents = outboxEventJpaRepository.findByPublishedFalse();
        if (unpublishedEvents.isEmpty()) {
            return;
        }
        log.info("Fetching unpublished events from outbox, found {} events", unpublishedEvents.size());
        unpublishedEvents.forEach(event -> {
            String topic = getTopicForEventType(event.getEventType());
            log.info("Going to publish event of type {} to kafka topic: {}", event.getEventType(), topic);
            kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload())
                    .whenComplete((_, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish event to kafka topic: {}", topic, ex);
                            return;
                        }
                        event.setPublished(true);
                        log.info("Finished publishing event to kafka topic: {}", topic);
                    }).join();
        });
        outboxEventJpaRepository.saveAll(unpublishedEvents);
    }

    private String getTopicForEventType(String eventType) {
        return switch (eventType) {
            case StockInitializedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getStockInitializedTopic();
            case StockReplenishedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getStockReplenishedTopic();
            case StockReservedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getStockReservedTopic();
            case StockDepletedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getStockDepletedTopic();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
