package com.spring.luispa.ecommerce_api.shared.exception;

public class PaymentGatewayException extends InfrastructureException {

    private final String transactionId;
    private final String gatewayResponse;

    public PaymentGatewayException(String transactionId, String gatewayResponse, Throwable cause) {
        super(String.format("Payment gateway error for transaction %s: %s",
                        transactionId, gatewayResponse),
                "PAYMENT_GATEWAY_ERROR",
                502,
                cause);
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }
}
