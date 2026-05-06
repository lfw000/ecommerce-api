package com.spring.luispa.ecommerce_api.api.dto.response;

import com.spring.luispa.ecommerce_api.domain.product.ProductAttributes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Product information response")
public class ProductResponse {

    @Schema(description = "Product ID",
            example = "1")
    private Long id;

    @Schema(description = "Unique SKU",
            example = "LAP-001")
    private String sku;

    @Schema(description = "Product name",
            example = "Pro Gaming Laptop")
    private String name;

    @Schema(description = "Product description",
            example = "High-end gaming laptop with Intel i9")
    private String description;

    @Schema(description = "Price",
            example = "1599.99")
    private BigDecimal price;

    @Schema(description = "Available stock",
            example = "50")
    private Integer stock;

    @Schema(description = "Category ID",
            example = "1")
    private Long categoryId;

    @Schema(description = "Category name",
            example = "Electronics")
    private String categoryName;

    @Schema(description = "Whether product is active",
            example = "true")
    private boolean active;

    @Schema(description = "Whether product is featured",
            example = "false")
    private boolean featured;

    @Schema(description = "Product attributes",
            example = "{\"color\":\"red\"}")
    private ProductAttributes attributes;

    @Schema(description = "List of product images")
    private List<ProductImageResponse> images;

    //@Schema(description = "Product dimensions (L×W×H in cm)",
    //        example = "{\"length\": 35, \"width\": 25, \"height\": 3}")
    //private Map<String, BigDecimal> dimensions;

    @Schema(description = "Creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date", example = "2024-01-20T14:25:00")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ProductAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ProductAttributes attributes) {
        this.attributes = attributes;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public List<ProductImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ProductImageResponse> images) {
        this.images = images;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
