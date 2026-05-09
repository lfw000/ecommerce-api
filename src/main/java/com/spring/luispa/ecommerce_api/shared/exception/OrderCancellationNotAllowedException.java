package com.spring.luispa.ecommerce_api.shared.exception;

import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;

public class OrderCancellationNotAllowedException extends DomainException {

    public OrderCancellationNotAllowedException(OrderStatus status) {
        super(
                "Cannot cancel order with status: " + status,
                "DOMAIN_ORDER_CANCELLATION_NOT_ALLOWED",
                409);
    }

    public OrderCancellationNotAllowedException(String message) {
        super(message, "DOMAIN_ORDER_CANCELLATION_NOT_ALLOWED", 409);
    }
}
