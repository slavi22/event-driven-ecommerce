package com.eventdriven.payment.adapter.in.messaging.kafka;

import com.eventdriven.contracts.order.event.OrderReadyForPaymentEventPayload;
import com.eventdriven.payment.application.command.ProcessPaymentCommand;
import com.eventdriven.payment.application.port.in.command.ProcessPaymentUseCase;
import com.eventdriven.payment.application.port.out.command.GetPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class OrderReadyForPaymentKafkaListener {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final GetPaymentPort getPaymentPort;

    @KafkaListener(topics = "${kafka.topics.order-ready-for-payment-topic}",
            groupId = "${kafka.config.consumer.groups.payment-order-ready-for-payment-group}",
            properties = "spring.json.value.default.type=com.eventdriven.contracts.order.event.OrderReadyForPaymentEventPayload")
    public void onOrderReadyForPaymentEvent(@Payload OrderReadyForPaymentEventPayload payload) {
        UUID orderId = UUID.fromString(payload.orderId());
        if (getPaymentPort.findByOrderId(orderId).isPresent()) {
            log.warn("Duplicate OrderReadyForPayment received for order {}, skipping", orderId);
            return;
        }
        log.info("Received OrderReadyForPaymentEvent for orderId: {}", orderId);
        processPaymentUseCase.processPayment(new ProcessPaymentCommand(orderId, payload.amount()));
    }

    @DltHandler
    public void onOrderReadyForPaymentDlt(@Payload OrderReadyForPaymentEventPayload payload,
                                          Exception ex) {
        log.error("DLT: OrderReadyForPayment for order {} failed: {}", payload.orderId(), ex.getMessage());
    }
}
