package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response containing shopping cart data")
public class CartResponse {

    @Schema(description = "Cart ID", example = "1")
    private Long id;

    @Schema(description = "Owner user ID", example = "1")
    private Long userId;

    @Schema(description = "Items in the cart")
    private List<CartItemResponse> items;

    @Schema(description = "Total amount", example = "1299.98")
    private BigDecimal totalAmount;

    @Schema(description = "Total number of items", example = "3")
    private int totalItems;

    @Schema(description = "Active cart", example = "true")
    private boolean active;

    @Schema(description = "Expiration date", example = "2024-02-15T10:30:00")
    private LocalDateTime expiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
