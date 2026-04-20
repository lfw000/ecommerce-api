package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request to update the quantity in the cart")
public class UpdateCartItemRequest {

    @Schema(description = "Product ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "New quantity (0 to delete)",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0")
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
