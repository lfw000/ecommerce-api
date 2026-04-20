package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Purchase order item")
public class OrderItemResponse {

    @Schema(description = "Product ID",
            example = "1")
    private Long productId;

    @Schema(description = "Product SKU",
            example = "LAP-001")
    private String productSku;

    @Schema(description = "Product name",
            example = "Laptop Gamer Pro")
    private String productName;

    @Schema(description = "Quantity",
            example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at time of purchase",
            example = "799.99")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal",
            example = "1599.98")
    private BigDecimal subtotal;

    @Schema(description = "Final price (after discounts)",
            example = "1599.98")
    private BigDecimal finalPrice;

    @Schema(description = "Discount percentage applied",
            example = "0")
    private Integer discountPercentage;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
