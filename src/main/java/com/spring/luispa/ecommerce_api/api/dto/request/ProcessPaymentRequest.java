package com.spring.luispa.ecommerce_api.api.dto.request;

import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object to process a payment")
public class ProcessPaymentRequest {

    @Schema(description = "Payment method",
        example = "CREDIT_CARD",
        allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(description = "Currency code (ISO 4217)",
            example = "USD",
            defaultValue = "USD",
            pattern = "^[A-Z]{3}$")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency = "USD";

    // Optional fields for card payments

    @Schema(description = "Last 4 digits of the card (for card payments) ",
        example = "4242")
    @Size(min = 4, max = 4, message = "Card last four must be exactly 4 digits")
    private String cardLastFour;

    @Schema(description = "Card brand",
        example = "VISA")
    private String cardBrand;

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

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
}
