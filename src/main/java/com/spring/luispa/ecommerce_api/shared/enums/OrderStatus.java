package com.spring.luispa.ecommerce_api.shared.enums;

public enum OrderStatus {
    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED;

    public boolean isActive() {
        return this == PENDING || this == PAID || this == PROCESSING;
    }

    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED || this == REFUNDED;
    }
}
