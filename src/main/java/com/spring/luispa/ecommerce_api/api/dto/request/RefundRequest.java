package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Refund request")
public class RefundRequest {

    @Schema(description = "Reason for refund",
        example = "Defective product",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Reason is required")
    private String reason;

    @Schema(description = "Amount to be refunded (if null, full refund)",
        example = "50.00")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

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
}
