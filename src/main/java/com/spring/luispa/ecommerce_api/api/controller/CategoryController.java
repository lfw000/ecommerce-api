package com.spring.luispa.ecommerce_api.api.controller;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.CategoryResponse;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Product category management")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List active categories", description = "Retrieve all active categories sorted by displayOrder")
    @ApiResponse(responseCode = "200", description = "List of categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.findAllActiveOrdered());
    }

    @GetMapping("/roots")
    @Operation(summary = "List root categories", description = "Retrieves categories that have no parent (top-level")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.findRootCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns the details of a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.findById(id));
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "List subcategories", description = "Retrieves the child categories of a parent category")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @Parameter(description = "Parent category ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.findSubcategories(id));
    }

    @GetMapping("/{id}/path")
    @Operation(summary = "Get full path", description = "Returns full category hierarchy (e.g., Electronics -> Computers -> Laptops)")
    public ResponseEntity<String> getCategoryPath(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.getCategoryPath(id));
    }

    // Administrator endpoints

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create category (admin)")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update category (admin)", description = "Updates the data for an existing category. Requires the ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Duplicate name")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category (admin)", description = "Soft-deletes a category. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted"),
            @ApiResponse(responseCode = "400", description = "Cannot delete a category with subcategories or products"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id) {
                categoryService.deleteCategory(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move category (admin)", description = "Changes the parent category of a category. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category moved"),
            @ApiResponse(responseCode = "400", description = "Cannot move to its own descendant"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> moveCategory(
            @Parameter(description = "ID of the category to move")
            @PathVariable Long id,
            @Parameter(description = "ID of the new parent category (null for root)")
            @RequestParam(required = false) Long newParentId) {
        categoryService.moveCategory(id, newParentId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
