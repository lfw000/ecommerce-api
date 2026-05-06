package com.spring.luispa.ecommerce_api.shared.exception;

public class ProductNotActiveException extends DomainException {

    public ProductNotActiveException(Long productId, String sku) {
        super(String.format("Product %s (ID: %d) is not available for purchase",
                sku, productId),
                "PRODUCT_NOT_ACTIVE",
                400);
    }
}
