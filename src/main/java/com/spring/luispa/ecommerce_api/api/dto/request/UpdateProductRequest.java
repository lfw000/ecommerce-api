package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Product update request")
public class UpdateProductRequest {

    @Schema(description = "New product name",
        example = "Ultra Gaming Laptop")
    @Size(max = 100)
    private String name;

    @Schema(description = "New product description")
    @Size(max = 2000)
    private String description;

    @Schema(description = "New price",
        example = "1799.99")
    @Positive
    private BigDecimal price;

    @Schema(description = "New stock",
            example = "25")
    @PositiveOrZero
    private Integer stock;

    @Schema(description = "New category ID",
        example = "2")
    private Long categoryId;

    @Schema(description = "Enable/disable product",
        example = "true")
    private Boolean active;

    @Schema(description = "Mark as featured",
            example = "true")
    private Boolean featured;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
}
