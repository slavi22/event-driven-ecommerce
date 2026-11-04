package com.eventdriven.stock.application.service.command;

import com.eventdriven.stock.application.command.InitializeStockCommand;
import com.eventdriven.stock.application.port.out.command.GetStockCommandPort;
import com.eventdriven.stock.application.port.out.command.SaveStockPort;
import com.eventdriven.stock.application.port.out.outbox.OutboxEvent;
import com.eventdriven.stock.application.port.out.outbox.SaveOutboxEventPort;
import com.eventdriven.stock.domain.entity.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitializeStockServiceTest {

    @Mock
    private GetStockCommandPort getStockCommandPort;
    @Mock
    private SaveStockPort saveStockPort;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @InjectMocks
    private InitializeStockService initializeStockService;

    @Test
    @DisplayName("Given stock does not exist, when initializing stock, then should save stock and outbox event")
    void testInitializeStock_whenStockDoesNotExist_shouldSaveStockAndOutboxEvent() {
        // Arrange
        UUID productId = UUID.randomUUID();
        InitializeStockCommand command = new InitializeStockCommand(productId, 50);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.empty());
        when(saveStockPort.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        initializeStockService.initializeStock(command);

        // Assert
        verify(saveStockPort, times(1)).save(any(Stock.class));
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Given stock already exists, when initializing stock, then should skip and not save anything")
    void testInitializeStock_whenStockAlreadyExists_shouldSkipAndNotSaveAnything() {
        // Arrange
        UUID productId = UUID.randomUUID();
        InitializeStockCommand command = new InitializeStockCommand(productId, 50);
        when(getStockCommandPort.getStockByProductId(productId)).thenReturn(Optional.of(mock(Stock.class)));

        // Act
        initializeStockService.initializeStock(command);

        // Assert
        verify(saveStockPort, never()).save(any(Stock.class));
        verify(saveOutboxEventPort, never()).save(any(OutboxEvent.class));
    }
}
