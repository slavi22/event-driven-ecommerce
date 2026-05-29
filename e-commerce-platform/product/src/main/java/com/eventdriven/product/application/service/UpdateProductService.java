package com.eventdriven.product.application.service;

import com.eventdriven.product.application.command.UpdateProductCommand;
import com.eventdriven.product.application.port.out.persistence.outbox.OutboxEvent;
import com.eventdriven.product.application.dto.UpdateProductResult;
import com.eventdriven.product.application.mapper.ProductApplicationMapper;
import com.eventdriven.product.application.port.in.UpdateProductUseCase;
import com.eventdriven.product.application.port.out.persistence.command.GetProductCommandPort;
import com.eventdriven.product.application.port.out.persistence.outbox.SaveOutboxEventPort;
import com.eventdriven.product.application.port.out.persistence.command.UpdateProductPort;
import com.eventdriven.product.domain.entity.Product;
import com.eventdriven.product.domain.event.ProductUpdatedEventPayload;
import com.eventdriven.product.domain.valueobject.Money;
import com.eventdriven.product.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UpdateProductService implements UpdateProductUseCase {

    private final GetProductCommandPort getProductCommandPort;
    private final UpdateProductPort updateProductPort;
    private final ProductApplicationMapper productApplicationMapper;
    private final SaveOutboxEventPort saveOutboxEventPort;
    private final JsonMapper jsonMapper;

    @Override
    @Transactional
    public UpdateProductResult updateProduct(UpdateProductCommand command) {
        log.info("Updating product with id: {}", command.productId());
        Product product =
                getProductCommandPort.getProductByProductId(new ProductId(UUID.fromString(command.productId())));

        product.update(command.name(), command.description(), Money.of(command.price()), command.category());

        log.info("Saving updated product with id: {}", command.productId());
        Product updatedProduct = updateProductPort.update(product);
        log.info("Product with id: {} updated successfully", command.productId());

        log.info("Saving updated product with id: {} to outbox table", command.productId());
        ProductUpdatedEventPayload payload = new ProductUpdatedEventPayload(
                updatedProduct.getId().getValue().toString(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice().getAmount(),
                updatedProduct.getCategory(),
                updatedProduct.getStatus(),
                updatedProduct.getCreatedAt(),
                updatedProduct.getUpdatedAt()
        );
        String jsonPayload = jsonMapper.writeValueAsString(payload);
        saveOutboxEventPort.save(new OutboxEvent(updatedProduct.getId().getValue().toString(),
                                                 ProductUpdatedEventPayload.AGGREGATE_TYPE,
                                                 ProductUpdatedEventPayload.EVENT_TYPE,
                                                 jsonPayload,
                                                 Instant.now()));
        log.info("Updated product with id: {} saved to outbox table successfully", command.productId());

        return productApplicationMapper.toUpdateProductResult(updatedProduct);
    }
}
