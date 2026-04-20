package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Cart item")
public class CartItemResponse {

    @Schema(description = "Product ID", example = "1")
    private Long productId;

    @Schema(description = "Product SKU", example = "LAP-001")
    private String productSku;

    @Schema(description = "Product Name", example = "Pro Gaming Laptop")
    private String productName;

    @Schema(description = "Product image URL",
        example = "https://cdn.ecommerce.com/images/laptop.jpg")
    private String productImage;

    @Schema(description = "Quantity", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at the time of adding", example = "799.99")
    private BigDecimal unitPrice;

    @Schema(description = "Subtotal (unitPrice × quantity)", example = "1599.98")
    private BigDecimal subtotal;

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

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
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
}
