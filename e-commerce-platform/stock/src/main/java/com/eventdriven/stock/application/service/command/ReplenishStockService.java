package com.eventdriven.stock.application.service.command;

import com.eventdriven.contracts.stock.event.StockReplenishedEventPayload;
import com.eventdriven.stock.application.command.ReplenishStockCommand;
import com.eventdriven.stock.application.dto.ReplenishStockResult;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.mapper.StockApplicationMapper;
import com.eventdriven.stock.application.port.in.ReplenishStockUseCase;
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
class ReplenishStockService implements ReplenishStockUseCase {

    private final GetStockCommandPort getStockCommandPort;
    private final UpdateStockPort updateStockPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final StockApplicationMapper stockApplicationMapper;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public ReplenishStockResult replenishStock(ReplenishStockCommand command) {
        log.info("Replenishing stock for product with id: {}", command.productId());
        Stock stock = getStockCommandPort.getStockByProductId(command.productId())
                .orElseThrow(() -> new StockNotFoundException(
                        "Stock for product with id " + command.productId() + " not found!"));

        stock.replenish(command.amount());

        log.info("Saving replenished stock for product with id: {}", command.productId());
        Stock updatedStock = updateStockPort.update(stock);
        log.info("Stock for product with id: {} replenished successfully", command.productId());

        log.info("Saving replenished stock for product with id: {} to outbox table", command.productId());
        StockReplenishedEventPayload payload = new StockReplenishedEventPayload(
                updatedStock.getId().getValue().toString(),
                updatedStock.getProductId().toString(),
                command.amount(),
                updatedStock.getQuantity().getValue(),
                Instant.now()
        );
        String jsonPayload = jsonMapper.writeValueAsString(payload);
        saveOutboxEventPort.save(new OutboxEvent(
                updatedStock.getId().getValue().toString(),
                StockReplenishedEventPayload.AGGREGATE_TYPE,
                StockReplenishedEventPayload.EVENT_TYPE,
                jsonPayload,
                Instant.now()
        ));
        log.info("Stock for product with id: {} saved to outbox table successfully", command.productId());
        return stockApplicationMapper.toReplenishStockResult(updatedStock);
    }
}
