package com.eventdriven.order.adapter.in.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
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
class PaymentFailedKafkaListener {

    private final OrderSagaOrchestrator orderSagaOrchestrator;

    @KafkaListener(topics = "${kafka.topics.payment-failed-topic}",
            groupId = "${kafka.config.consumer.groups.payment-failed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.payment.event.PaymentFailedEventPayload")
    @Transactional
    public void onPaymentFailed(@Payload PaymentFailedEventPayload payload) {
        log.info("Received PaymentFailedEvent for orderId: {}", payload.orderId());
        orderSagaOrchestrator.onPaymentFailed(payload);
    }

    @DltHandler
    public void onPaymentFailedDlt(@Payload PaymentFailedEventPayload payload,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                   @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process PaymentFailedEvent for orderId: {}, originalTopic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
