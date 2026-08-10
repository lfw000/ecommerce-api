package com.spring.luispa.ecommerce_api.services.validation;

import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.domain.product.ProductRepository;
import com.spring.luispa.ecommerce_api.shared.exception.DuplicateResourceException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductValidator {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductValidator(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public void validateSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU cannot be null or blank");
        }
    }

    public void validateSkuUniqueness(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("Product already exists with SKU: " + sku);
        }
    }

    public Category validateCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    public void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    public void validateStock(Integer stock) {
        if (stock == null || stock <= 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}
