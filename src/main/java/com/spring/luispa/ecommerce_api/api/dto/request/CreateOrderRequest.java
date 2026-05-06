package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object to create an order from cart")
public class CreateOrderRequest {

    @Schema(description = "ID of the shipping address",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    @Schema(description = "ID of the billing address",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Billing address ID is required")
    private Long billingAddressId;

    @Schema(description = "Shipping method",
        example = "express",
        allowableValues = {"standard", "express", "overnight"})
    private String shippingMethod;

    @Schema(description = "Additional order notes",
        example = "Please call before delivery")
    private String notes;

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public Long getBillingAddressId() {
        return billingAddressId;
    }

    public void setBillingAddressId(Long billingAddressId) {
        this.billingAddressId = billingAddressId;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
