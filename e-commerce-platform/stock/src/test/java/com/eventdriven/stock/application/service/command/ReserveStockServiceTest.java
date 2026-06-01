package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.ReserveStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.exception.StockDomainException;
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
class ReserveStockServiceTest {

    @Mock
    private GetStockCommandPort getStockCommandPort;
    @Mock
    private UpdateStockPort updateStockPort;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;
    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ReserveStockService reserveStockService;

    @Test
    @DisplayName("Given sufficient stock, when reserving, then should update stock and save StockReserved outbox event")
    void testReserveStock_withSufficientStock_shouldUpdateAndSaveStockReservedEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Stock existingStock = buildStock(productId, 100);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(existingStock));
        when(updateStockPort.update(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        reserveStockService.reserveStock(new ReserveStockCommand(orderId, productId, 30));

        // Assert — quantity not zero, so only StockReserved is saved
        verify(updateStockPort, times(1)).update(any(Stock.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Given stock that hits zero after reservation, when reserving, then should also save StockDepleted outbox event")
    void testReserveStock_whenQuantityHitsZero_shouldSaveStockDepletedEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Stock existingStock = buildStock(productId, 10);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(existingStock));
        when(updateStockPort.update(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        reserveStockService.reserveStock(new ReserveStockCommand(orderId, productId, 10));

        // Assert — StockReserved + StockDepleted
        verify(saveOutboxEventPort, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Given stock does not exist, when reserving, then should throw StockNotFoundException")
    void testReserveStock_whenStockNotFound_shouldThrowStockNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockNotFoundException.class,
                () -> reserveStockService.reserveStock(new ReserveStockCommand(UUID.randomUUID(), productId, 5)));

        verify(updateStockPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
    }

    @Test
    @DisplayName("Given insufficient stock, when reserving, then should throw StockDomainException")
    void testReserveStock_withInsufficientStock_shouldThrowStockDomainException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Stock existingStock = buildStock(productId, 3);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(existingStock));

        // Act & Assert
        assertThrows(StockDomainException.class,
                () -> reserveStockService.reserveStock(new ReserveStockCommand(UUID.randomUUID(), productId, 10)));

        verify(updateStockPort, never()).update(any());
        verify(saveOutboxEventPort, never()).save(any());
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
