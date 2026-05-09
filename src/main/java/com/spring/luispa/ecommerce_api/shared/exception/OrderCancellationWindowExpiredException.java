package com.spring.luispa.ecommerce_api.shared.exception;

public class OrderCancellationWindowExpiredException extends DomainException {

    public OrderCancellationWindowExpiredException() {
        super(
                "Cancellation window has expired",
                "DOMAIN_ORDER_CANCELLATION_WINDOW_EXPIRED",
                409
        );
    }

    public OrderCancellationWindowExpiredException(String message) {
        super(
                message,
                "DOMAIN_ORDER_CANCELLATION_WINDOW_EXPIRED",
                409
        );
    }
}
