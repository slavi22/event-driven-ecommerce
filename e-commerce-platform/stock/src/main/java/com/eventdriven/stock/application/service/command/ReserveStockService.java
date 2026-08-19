package com.eventdriven.stock.application.service.command;

import com.eventdriven.contracts.stock.event.StockDepletedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservationFailedEventPayload;
import com.eventdriven.contracts.stock.event.StockReservedEventPayload;
import com.eventdriven.stock.application.command.ReserveStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.port.in.ReserveStockUseCase;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.exception.StockDomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
class ReserveStockService implements ReserveStockUseCase {

    private final GetStockCommandPort getStockCommandPort;
    private final UpdateStockPort updateStockPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void reserveStock(ReserveStockCommand command) {
        log.info("Reserving {} units of stock for product {} (order {})",
                command.amount(), command.productId(), command.orderId());

        Stock stock = getStockCommandPort.getStockByProductId(command.productId())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock for product with id " + command.productId() + " not found!"));

        try {
            stock.reserve(command.amount());
        } catch (StockDomainException e) {
            log.warn("Stock reservation failed for product {} (order {}): {}",
                    command.productId(), command.orderId(), e.getMessage());
            saveOutboxEventPort.save(new OutboxEvent(
                    stock.getId().getValue().toString(),
                    StockReservationFailedEventPayload.AGGREGATE_TYPE,
                    StockReservationFailedEventPayload.EVENT_TYPE,
                    jsonMapper.writeValueAsString(new StockReservationFailedEventPayload(
                            command.orderId().toString(),
                            command.productId().toString(),
                            e.getMessage(),
                            Instant.now()
                    )),
                    Instant.now()
            ));
            return;
        }

        Stock updatedStock = updateStockPort.update(stock);
        log.info("Reserved {} units for product {}, remaining quantity: {}",
                command.amount(), command.productId(), updatedStock.getQuantity().getValue());

        saveOutboxEventPort.save(new OutboxEvent(
                updatedStock.getId().getValue().toString(),
                StockReservedEventPayload.AGGREGATE_TYPE,
                StockReservedEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new StockReservedEventPayload(
                        updatedStock.getId().getValue().toString(),
                        updatedStock.getProductId().toString(),
                        command.orderId().toString(),
                        command.amount(),
                        updatedStock.getQuantity().getValue(),
                        Instant.now()
                )),
                Instant.now()
        ));

        if (updatedStock.getQuantity().getValue() == 0) {
            log.info("Stock for product {} is now depleted, saving StockDepleted event to outbox",
                    command.productId());
            saveOutboxEventPort.save(new OutboxEvent(
                    updatedStock.getId().getValue().toString(),
                    StockDepletedEventPayload.AGGREGATE_TYPE,
                    StockDepletedEventPayload.EVENT_TYPE,
                    jsonMapper.writeValueAsString(new StockDepletedEventPayload(
                            updatedStock.getId().getValue().toString(),
                            updatedStock.getProductId().toString(),
                            Instant.now()
                    )),
                    Instant.now()
            ));
        }
    }
}
