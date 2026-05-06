package com.spring.luispa.ecommerce_api.shared.exception;

public class PaymentProcessingException extends BaseException {

    private final String transactionId;
    private final String gatewayError;

    public PaymentProcessingException(String message, String transactionId, String gatewayError) {
        super(message, "PAYMENT_PROCESSING_FAILED", 400);
        this.transactionId = transactionId;
        this.gatewayError = gatewayError;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGatewayError() {
        return gatewayError;
    }
}
