package com.spring.luispa.ecommerce_api.shared.exception;

public class OrderCancellationWindowExpiredException extends DomainException {

    public OrderCancellationWindowExpiredException() {
        super(
                "Cancellation window has expired",
                "ORDER_CANCELLATION_WINDOW_EXPIRED",
                409
        );
    }

    public OrderCancellationWindowExpiredException(String message) {
        super(
                message,
                "ORDER_CANCELLATION_WINDOW_EXPIRED",
                409
        );
    }
}
