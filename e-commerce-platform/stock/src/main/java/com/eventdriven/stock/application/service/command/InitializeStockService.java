package com.eventdriven.stock.application.service.command;

import com.eventdriven.contracts.stock.event.StockInitializedEventPayload;
import com.eventdriven.stock.application.command.InitializeStockCommand;
import com.eventdriven.stock.application.port.in.InitializeStockUseCase;
import com.eventdriven.stock.application.port.out.command.SaveStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.valueobject.Quantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
class InitializeStockService implements InitializeStockUseCase {

    private final SaveStockPort saveStockPort;
    private final JsonMapper jsonMapper;
    private final SaveOutboxEventPort saveOutboxEventPort;

    @Override
    public void initializeStock(InitializeStockCommand command) {
        log.info("Initializing stock for product with id: {}", command.productId());
        Stock newStock = Stock.initialize(command.productId(), command.initialQuantity());
        log.info("Saving stock for product with id: {}", command.productId());
        saveStockPort.save(newStock);
        log.info("Stock for product with id: {} saved successfully", command.productId());

        log.info("Saving stock for product with id: {} to outbox table", command.productId());
        StockInitializedEventPayload payload = new StockInitializedEventPayload(
                newStock.getId().getValue().toString(),
                newStock.getProductId().toString(),
                newStock.getQuantity().getValue(),
                newStock.getCreatedAt()
        );
        String jsonPayload = jsonMapper.writeValueAsString(payload);
        saveOutboxEventPort.save(new OutboxEvent(newStock.getId().getValue().toString(),
                                                 StockInitializedEventPayload.AGGREGATE_TYPE,
                                                 StockInitializedEventPayload.EVENT_TYPE,
                                                 jsonPayload,
                                                 Instant.now()));
        log.info("Stock for product with id: {} saved to outbox table successfully", command.productId());
    }
}
