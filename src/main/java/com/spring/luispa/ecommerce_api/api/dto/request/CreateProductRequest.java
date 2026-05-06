package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Request object to create a new product")
public class CreateProductRequest {

    @Schema(description = "Unique product SKU (Stock Keeping Unit)",
        example = "LAP-001",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @Schema(description = "Product name",
        example = "Gamer Pro Laptop",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @Schema(description = "Detailed product description",
        example = "High-end laptop with an Intel i9 processor, 32GB RAM, and a 1TB SSD")
    @Size(max = 2000)
    private String description;

    @Schema(description = "Product price",
        example = "1599.99",
        requiredMode =  Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Schema(description = "Initial stock quantity",
        example = "50",
        defaultValue = "0")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock = 0;

    @Schema(description = "Category ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Schema(description = "Indicates whether the product is featured",
            example = "false",
            defaultValue = "false")
    private boolean featured = false;

    @Schema(description = "Product attributes",
            example = "{\"color\": \"red\",\"size\":32}",
            defaultValue = "{}")
    private Map<String, String> attributes;

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

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
