package com.spring.luispa.ecommerce_api.shared.enums;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    CANCELLED;

    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    public boolean isRefundable() {
        return this == COMPLETED || this == PARTIALLY_REFUNDED;
    }
}
