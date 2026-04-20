package com.spring.luispa.ecommerce_api.api.dto.request;

import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payment processing request")
public class ProcessPaymentRequest {

    @Schema(description = "Payment method",
        example = "CREDIT_CARD",
        allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment currency",
            example = "USD",
            pattern = "^[A-Z]{3}$"
    )
    private String currency = "USD";

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
