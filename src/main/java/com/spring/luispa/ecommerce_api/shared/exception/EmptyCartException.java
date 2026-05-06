package com.spring.luispa.ecommerce_api.shared.exception;

public class EmptyCartException extends DomainException {

    public EmptyCartException(Long userId) {
        super(String.format("Cannot create order. Cart is empty for user %d", userId),
                "EMPTY_CART",
                400);
    }
}
