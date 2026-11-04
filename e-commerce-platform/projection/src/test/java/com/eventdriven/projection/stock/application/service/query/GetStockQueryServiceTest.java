package com.eventdriven.projection.stock.application.service.query;

import com.eventdriven.projection.stock.application.dto.GetStockResult;
import com.eventdriven.projection.stock.application.exception.StockNotFoundException;
import com.eventdriven.projection.stock.application.port.out.query.GetStockQueryPort;
import com.eventdriven.projection.stock.application.query.GetStockQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStockQueryServiceTest {

    @Mock
    private GetStockQueryPort getStockQueryPort;

    @InjectMocks
    private GetStockQueryService getStockQueryService;

    @Test
    @DisplayName("Getting stock for an existing product should return the stock result")
    void testGetStock_withExistingProduct_shouldReturnGetStockResult() {
        // Arrange
        UUID productId = UUID.randomUUID();
        GetStockResult expected = new GetStockResult(UUID.randomUUID(), productId, 100, Instant.now(), null);
        when(getStockQueryPort.getStockByProductId(productId)).thenReturn(Optional.of(expected));

        // Act
        GetStockResult result = getStockQueryService.getStockByProductId(new GetStockQuery(productId));

        // Assert
        assertEquals(expected, result);
        verify(getStockQueryPort).getStockByProductId(productId);
    }

    @Test
    @DisplayName("Getting stock for a non-existing product should throw StockNotFoundException")
    void testGetStock_withNonExistingProduct_shouldThrowStockNotFoundException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(getStockQueryPort.getStockByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StockNotFoundException.class,
                () -> getStockQueryService.getStockByProductId(new GetStockQuery(productId)));
        verify(getStockQueryPort).getStockByProductId(productId);
    }
}
