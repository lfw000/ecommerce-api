package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product image information")
public class ProductImageResponse {

    @Schema(description = "Image ID",
            example = "1")
    private Long id;

    @Schema(description = "Image URL",
        example = "https://cdn.ecommerce.com/images/product-1.jpg")
    private String imageUrl;

    @Schema(description = "SEO alt text",
            example = "Pro Gaming Laptop - Front View")
    private String altText;

    @Schema(description = "Display order",
            example = "0")
    private Integer displayOrder;

    @Schema(description = "Whether this is the main image",
            example = "true")
    private boolean main;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isMain() {
        return main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }
}