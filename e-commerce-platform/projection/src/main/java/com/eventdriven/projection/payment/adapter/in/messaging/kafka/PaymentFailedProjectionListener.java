package com.eventdriven.projection.payment.adapter.in.messaging.kafka;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.projection.payment.application.dto.GetPaymentResult;
import com.eventdriven.projection.payment.application.port.out.query.GetPaymentQueryPort;
import com.eventdriven.projection.payment.application.port.out.query.SavePaymentQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class PaymentFailedProjectionListener {

    private final GetPaymentQueryPort getPaymentQueryPort;
    private final SavePaymentQueryPort savePaymentQueryPort;

    @KafkaListener(
            topics = "${kafka.topics.payment-failed-topic}",
            groupId = "${kafka.config.consumer.groups.payment-failed-events-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.payment.event.PaymentFailedEventPayload"
    )
    public void onPaymentFailed(@Payload PaymentFailedEventPayload payload,
                                @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        UUID orderId = UUID.fromString(payload.orderId());
        log.info("Received PaymentFailedEvent for order: {}", orderId);
        if (getPaymentQueryPort.getPaymentByOrderId(orderId).isPresent()) {
            log.warn("Payment projection for order {} already exists, skipping", orderId);
            return;
        }
        savePaymentQueryPort.save(new GetPaymentResult(
                orderId, null, "FAILED", payload.reason(),
                payload.occurredOn(), payload.occurredOn()));
        log.info("Payment read model (failed) created for order: {}", orderId);
    }

    @DltHandler
    public void onPaymentFailedDlt(@Payload PaymentFailedEventPayload payload,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                   @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionClass,
                                   @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic) {
        log.error("Failed to process PaymentFailedEvent for order: {}, topic: {}, exception: {} - {}",
                  payload.orderId(), originalTopic, exceptionClass, exceptionMessage);
    }
}
