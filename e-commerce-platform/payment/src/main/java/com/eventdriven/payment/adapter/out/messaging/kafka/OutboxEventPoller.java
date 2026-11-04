package com.eventdriven.payment.adapter.out.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.payment.adapter.out.messaging.kafka.config.KafkaTopicProperties;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventEntity;
import com.eventdriven.payment.adapter.out.persistence.command.postgres.outbox.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class OutboxEventPoller {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Scheduled(fixedDelayString = "${app.scheduling.payment}")
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
            case PaymentProcessedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getPaymentProcessedTopic();
            case PaymentFailedEventPayload.EVENT_TYPE -> kafkaTopicProperties.getPaymentFailedTopic();
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
