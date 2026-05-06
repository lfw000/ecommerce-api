package com.spring.luispa.ecommerce_api.api.dto.request;

import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object to cancel an order")
public class CancelOrderRequest {

    @Schema(description = "Reason for cancellation",
        example = "USER_REQUESTED",
        allowableValues = {"USER_REQUESTED", "PAYMENT_FAILED", "OUT_OF_STOCK",
            "ADMIN_CANCELLED", "FRAUD_SUSPECTED",
            "SHIPPING_UNAVAILABLE", "PRICE_ERROR"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Cancellation reason is required")
    private CancellationReason reason;

    @Schema(description = "Additional comments about cancellation",
        example = "Customer found a better price elsewhere",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;

    public CancelOrderRequest() {
    }

    public CancelOrderRequest(CancellationReason reason, String comment) {
        this.reason = reason;
        this.comment = comment;
    }

    public CancellationReason getReason() {
        return reason;
    }

    public void setReason(CancellationReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
