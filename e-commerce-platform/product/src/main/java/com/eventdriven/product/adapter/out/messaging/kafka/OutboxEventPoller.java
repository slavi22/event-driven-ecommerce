package com.eventdriven.product.adapter.out.messaging.kafka;

import com.eventdriven.product.adapter.out.messaging.kafka.config.KafkaTopicProperties;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
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

    @Scheduled(fixedDelayString = "${app.scheduling.product}")
    @Transactional // TODO: maybe add kafka transaction manager here ?
    public void readOutbox() {
        List<OutboxEventEntity> unpublishedEvents = outboxEventJpaRepository.findByPublishedFalse();
        if (unpublishedEvents.isEmpty()) {
            return;
        }
        log.info("Fetching unpublished events from outbox, found {} events", unpublishedEvents.size());
        unpublishedEvents.forEach(event -> {
            String topic = getTopicForEventType(event.getEventType());
            log.info("Going to publish events to kafka topic: {}", topic);

            kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
            event.setPublished(true);
            log.info("Finished publishing events to kafka topic: {}", kafkaTopicProperties.getProductCreatedTopic());
        });
        // we don't need to call saveAll here because the entities are managed by JPA and since we have @Transactioanal if everything goes ok it will be automatically updated
        // outboxEventJpaRepository.saveAll(unpublishedEvents);
    }

    private String getTopicForEventType(String eventType) {
        return switch (eventType) {
            case "ProductCreated" -> kafkaTopicProperties.getProductCreatedTopic();
            case "ProductUpdated" -> kafkaTopicProperties.getProductUpdatedTopic();
            case "ProductDeleted" -> kafkaTopicProperties.getProductDeletedTopic();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
