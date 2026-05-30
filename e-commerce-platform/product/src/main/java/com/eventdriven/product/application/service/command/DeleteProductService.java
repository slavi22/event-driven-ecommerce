package com.eventdriven.product.application.service.command;

import com.eventdriven.product.application.command.DeleteProductCommand;
import com.eventdriven.product.application.port.in.command.DeleteProductUseCase;
import com.eventdriven.product.application.port.out.persistence.command.DeleteProductPort;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.contracts.product.event.ProductDeletedEventPayload;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class DeleteProductService implements DeleteProductUseCase {

    private final GetProductCommandPort getProductCommandPort;
    private final DeleteProductPort deleteProductPort;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public void deleteProduct(DeleteProductCommand command) {
        log.info("Deleting (marking as inactive) product with ID: {}", command.productId());
        ProductId productId = new ProductId(command.productId());
        Product existingProduct = getProductCommandPort.getProductByProductId(productId);
        existingProduct.delete();
        log.info("Going to mark product with ID: {} as deleted (inactive) ", command.productId());
        deleteProductPort.deleteProductById(productId);
        log.info("Product with ID: {} marked as deleted (inactive) in the database", command.productId());
        log.info("Going to mark product with ID: {} as deleted (inactive) in the outbox table", command.productId());
        ProductDeletedEventPayload payload =
                new ProductDeletedEventPayload(existingProduct.getId().getValue().toString(),
                                               existingProduct.getName(),
                                               existingProduct.getDescription(),
                                               existingProduct.getPrice().getAmount(),
                                               existingProduct.getCategory(),
                                               existingProduct.getStatus(),
                                               existingProduct.getCreatedAt(),
                                               existingProduct.getUpdatedAt());
        String jsonPayload = jsonMapper.writeValueAsString(payload);
        saveOutboxEventPort.save(new OutboxEvent(productId.getValue().toString(),
                                                 ProductDeletedEventPayload.AGGREGATE_TYPE,
                                                 ProductDeletedEventPayload.EVENT_TYPE,
                                                 jsonPayload,
                                                 Instant.now()));
        log.info("Product with ID: {} marked as deleted (inactive) in the outbox table", command.productId());
    }
}
