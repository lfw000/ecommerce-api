package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new category")
public class CreateCategoryRequest {

    @Schema(description = "Category name",
            example = "Electronics",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 50)
    private String name;

    @Schema(description = "Category description",
        example = "Electronic products and gadgets")
    @Size(max = 200)
    private String description;

    @Schema(description = "Parent category ID (null for root category)",
        example = "null")
    private Long parentId;

    @Schema(description = "Display order",
            example = "1",
            defaultValue = "0")
    private Integer displayOrder;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
