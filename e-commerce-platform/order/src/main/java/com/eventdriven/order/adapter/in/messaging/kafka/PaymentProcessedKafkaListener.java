package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.order.application.service.command.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
class PaymentProcessedKafkaListener {

    private final OrderSagaOrchestrator orderSagaOrchestrator;

    @KafkaListener(topics = "${kafka.topics.payment-processed-topic}",
            groupId = "${kafka.config.consumer.groups.payment-processed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload")
    @Transactional
    public void onPaymentProcessed(@Payload PaymentProcessedEventPayload payload) {
        log.info("Received PaymentProcessedEvent for orderId: {}", payload.orderId());
        orderSagaOrchestrator.onPaymentProcessed(payload);
    }

    @DltHandler
    public void onPaymentProcessedDlt(@Payload PaymentProcessedEventPayload payload,
                                      @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                      @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                      @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process PaymentProcessedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
