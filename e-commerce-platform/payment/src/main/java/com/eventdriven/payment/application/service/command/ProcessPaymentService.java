package com.eventdriven.payment.application.service.command;

import com.eventdriven.contracts.payment.event.PaymentFailedEventPayload;
import com.eventdriven.contracts.payment.event.PaymentProcessedEventPayload;
import com.eventdriven.payment.application.command.ProcessPaymentCommand;
import com.eventdriven.payment.application.port.in.command.ProcessPaymentUseCase;
import com.eventdriven.payment.application.port.out.command.SavePaymentPort;
import com.eventdriven.payment.application.port.out.external.PaymentGatewayPort;
import com.eventdriven.payment.application.port.out.external.PaymentGatewayResult;
import com.eventdriven.payment.application.port.out.outbox.OutboxEvent;
import com.eventdriven.payment.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.payment.domain.entity.Payment;
import com.eventdriven.payment.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
class ProcessPaymentService implements ProcessPaymentUseCase {

    private final SavePaymentPort savePaymentPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final PaymentGatewayPort paymentGatewayPort;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void processPayment(ProcessPaymentCommand command) {
        Payment payment = Payment.initiate(command.orderId(), Money.of(command.amount()));

        PaymentGatewayResult result = paymentGatewayPort.charge(payment);

        if (result.success()) {
            payment.markProcessed();
            savePaymentPort.save(payment);
            saveOutboxEventPort.save(new OutboxEvent(
                    payment.getId().getValue().toString(),
                    PaymentProcessedEventPayload.AGGREGATE_TYPE,
                    PaymentProcessedEventPayload.EVENT_TYPE,
                    jsonMapper.writeValueAsString(
                            new PaymentProcessedEventPayload(command.orderId().toString(), command.amount(),
                                                             Instant.now())),
                    Instant.now()
            ));
        } else {
            payment.markFailed(result.reason());
            savePaymentPort.save(payment);
            saveOutboxEventPort.save(new OutboxEvent(
                    payment.getId().getValue().toString(),
                    PaymentFailedEventPayload.AGGREGATE_TYPE,
                    PaymentFailedEventPayload.EVENT_TYPE,
                    jsonMapper.writeValueAsString(new PaymentFailedEventPayload(
                            command.orderId().toString(),
                            result.reason(),
                            Instant.now()
                    )),
                    Instant.now()
            ));
        }

        log.info("Payment for order {} processed with status: {}", command.orderId(), payment.getStatus());
    }
}
