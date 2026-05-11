package com.eventdriven.product.adapter.out.messaging;

import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventEntity;
import com.eventdriven.product.adapter.out.persistance.postgres.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventPoller {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties kafkaTopicProperties;

    @Scheduled(fixedDelay = 10_000) // every 10 secs
    @Transactional
    public void readOutbox() {
        List<OutboxEventEntity> unpublishedEvents = outboxEventJpaRepository.findByPublishedFalse();
        unpublishedEvents.forEach(event -> {
            kafkaTemplate.send(kafkaTopicProperties.getProductCreatedTopic(), event.getAggregateId(), event.getPayload());
            event.setPublished(true);
        });
        // we don't need to call saveAll here because the entities are managed by JPA and since we have @Transactioanal if everything goes ok it will be automatically updated
        //outboxEventJpaRepository.saveAll(unpublishedEvents);
    }
}
