package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.ReleaseStockCommand;
import com.eventdriven.stock.application.exception.StockNotFoundException;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.UpdateStockPort;
import com.eventdriven.stock.domain.entity.Stock;
import com.eventdriven.stock.domain.valueobject.Quantity;
import com.eventdriven.stock.domain.valueobject.StockId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseStockServiceTest {

    @Mock
    private GetStockCommandPort getStockCommandPort;
    @Mock
    private UpdateStockPort updateStockPort;

    @InjectMocks
    private ReleaseStockService releaseStockService;

    @Test
    @DisplayName("Given stock exists, when releasing, then should update stock with increased quantity")
    void testReleaseStock_whenStockExists_shouldIncreaseQuantity() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Stock existingStock = buildStock(productId, 10);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(existingStock));
        when(updateStockPort.update(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        releaseStockService.releaseStock(new ReleaseStockCommand(UUID.randomUUID(), productId, 5));

        // Assert
        verify(updateStockPort, times(1)).update(any(Stock.class));
    }

    @Test
    @DisplayName("Given stock does not exist, when releasing, then should throw StockNotFoundException")
    void testReleaseStock_whenStockNotFound_shouldThrowStockNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockNotFoundException.class,
                () -> releaseStockService.releaseStock(new ReleaseStockCommand(UUID.randomUUID(), productId, 5)));

        verify(updateStockPort, never()).update(any());
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
