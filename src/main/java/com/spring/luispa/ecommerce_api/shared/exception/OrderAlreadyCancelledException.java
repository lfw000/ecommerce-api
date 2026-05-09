package com.spring.luispa.ecommerce_api.shared.exception;

public class OrderAlreadyCancelledException extends DomainException {

    public OrderAlreadyCancelledException() {
        super(
                "Order is already cancelled",
                "DOMAIN_ORDER_ALREADY_CANCELLED",
                409
        );
    }

    public OrderAlreadyCancelledException(String message) {
        super(
                message,
                "DOMAIN_ORDER_ALREADY_CANCELLED",
                409
        );
    }

}
