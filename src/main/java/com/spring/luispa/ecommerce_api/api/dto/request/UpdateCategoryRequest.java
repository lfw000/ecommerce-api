package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a category")
public class UpdateCategoryRequest {

    @Schema(description = "Category name", example = "Electronics")
    @Size(max = 50)
    private String name;

    @Schema(description = "Category description", example = "Electronic products and gadgets")
    @Size(max = 200)
    private String description;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean active;

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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
