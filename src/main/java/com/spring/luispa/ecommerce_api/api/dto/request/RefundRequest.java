package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request object to refund a payment")
public class RefundRequest {

    @Schema(description = "Reason for refund",
        example = "Customer requested cancellation",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    private String reason;

    @Schema(description = "Amount to be refunded (null or full amount = full refund)",
        example = "50.00")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Schema(description = "Whether to cancel the order after refund",
        example = "true",
        defaultValue = "false")
    private boolean cancelOrderAfterRefund = false;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isCancelOrderAfterRefund() {
        return cancelOrderAfterRefund;
    }

    public void setCancelOrderAfterRefund(boolean cancelOrderAfterRefund) {
        this.cancelOrderAfterRefund = cancelOrderAfterRefund;
    }
}
