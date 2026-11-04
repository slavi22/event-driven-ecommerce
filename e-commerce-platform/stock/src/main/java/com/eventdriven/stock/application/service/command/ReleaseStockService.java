package com.eventdriven.stock.application.service.command;

import com.eventdriven.contracts.stock.event.StockReleasedEventPayload;
import com.eventdriven.stock.application.command.ReleaseStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.port.in.ReleaseStockUseCase;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
class ReleaseStockService implements ReleaseStockUseCase {

    private final GetStockCommandPort getStockCommandPort;
    private final UpdateStockPort updateStockPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void releaseStock(ReleaseStockCommand command) {
        log.info("Releasing {} units of stock for product {} (order {})",
                command.amount(), command.productId(), command.orderId());

        Stock stock = getStockCommandPort.getStockByProductId(command.productId())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock for product with id " + command.productId() + " not found!"));

        stock.release(command.amount());

        Stock updatedStock = updateStockPort.update(stock);
        log.info("Released {} units for product {}, new quantity: {}",
                command.amount(), command.productId(), updatedStock.getQuantity().getValue());

        saveOutboxEventPort.save(new OutboxEvent(
                updatedStock.getId().getValue().toString(),
                StockReleasedEventPayload.AGGREGATE_TYPE,
                StockReleasedEventPayload.EVENT_TYPE,
                jsonMapper.writeValueAsString(new StockReleasedEventPayload(
                        command.orderId().toString(),
                        command.productId().toString(),
                        command.amount(),
                        Instant.now())),
                Instant.now()));
    }
}
