package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.ReplenishStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.mapper.StockApplicationMapper;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.valueobject.Quantity;
import com.eventdriven.stock.domain.valueobject.StockId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplenishStockServiceTest {

    @Mock
    private GetStockCommandPort getStockCommandPort;
    @Mock
    private UpdateStockPort updateStockPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private StockApplicationMapper stockApplicationMapper;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ReplenishStockService replenishStockService;

    @Test
    @DisplayName("Given stock exists, when replenishing, then should update stock and save outbox event")
    void testReplenishStock_whenStockExists_shouldUpdateStockAndSaveOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Stock existingStock = buildStock(productId, 50);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(existingStock));
        when(updateStockPort.update(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        replenishStockService.replenishStock(new ReplenishStockCommand(productId, 25));

        // Assert
        verify(updateStockPort, times(1)).update(any(Stock.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Given stock does not exist, when replenishing, then should throw StockNotFoundException")
    void testReplenishStock_whenStockNotFound_shouldThrowStockNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.empty());
        ReplenishStockCommand command = new ReplenishStockCommand(productId, 10);

        // Act & Assert
        assertThrows(StockNotFoundException.class, () -> replenishStockService.replenishStock(command));

        verify(updateStockPort, never()).update(any(Stock.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
    }

    private Stock buildStock(UUID productId, int quantity) {
        return Stock.reconstitute(
                new StockId(UUID.randomUUID()),
                productId,
                Quantity.of(quantity),
                Instant.now(),
                Instant.now()
        );
    }
}
