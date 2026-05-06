package com.spring.luispa.ecommerce_api.shared.exception;

public class InsufficientStockException extends DomainException {

    public InsufficientStockException(Long productId, String sku, int requested, int available) {
        super(
                String.format("Insufficient stock for product %s (ID: %d). Requested: %d, Available: %d",
                        sku, productId, requested, available),
                "INSUFFICIENT_STOCK",
                400);
    }
}
