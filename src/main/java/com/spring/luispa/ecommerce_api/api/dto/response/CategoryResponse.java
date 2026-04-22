package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category response")
public class CategoryResponse {

    @Schema(description = "Category ID", example = "1")
    private Long id;

    @Schema(description = "Category name", example = "Electrónicos")
    private String name;

    @Schema(description = "Category description", example = "Electronic products and gadgets")
    private String description;

    @Schema(description = "Whether the category is active", example = "true")
    private boolean active;

    @Schema(description = "Parent category ID", example = "null")
    private Long parentCategoryId;

    @Schema(description = "Parent category name", example = "null")
    private String parentCategoryName;

    @Schema(description = "Display order", example = "1")
    private Integer displayOrder;

    @Schema(description = "Full category path", example = "Electrónicos > Computadoras > Laptops")
    private String fullPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}
