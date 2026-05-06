package com.spring.luispa.ecommerce_api.shared.enums;

public enum CancellationReason {

    USER_REQUESTED("Customer requested cancellation"),
    PAYMENT_FAILED("Payment failed"),
    OUT_OF_STOCK("Product(s) out of stock"),
    ADMIN_CANCELLED("Administrator cancelled"),
    FRAUD_SUSPECTED("Suspected fraudulent transaction"),
    SHIPPING_UNAVAILABLE("No shipping available to address"),
    PRICE_ERROR("Pricing error requiring cancellation");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
