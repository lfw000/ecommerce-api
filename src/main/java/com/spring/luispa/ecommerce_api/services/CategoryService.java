package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllActive() {
        return categoryRepository.findByActiveTrue();
    }

    public List<Category> findAllActiveOrdered() {
        return categoryRepository.findAllActiveOrdered();
    }

    public List<Category> findRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public List<Category> findSubcategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId);
    }

    @Transactional
    public Category createCategory(String name, String description, Long parentId, Integer displayOrder) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException("Category already exists with name: " + name);
        }

        Category category = new Category(name);
        category.setDescription(description);
        category.setDisplayOrder(displayOrder != null ? displayOrder : 0);

        if (parentId != null) {
            Category parent = findById(parentId);
            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, Integer displayOrder, Boolean active) {
        Category category = findById(id);

        if (name != null && !name.isBlank()) {
            categoryRepository.findByName(name)
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessException("Category name already used: " + name);
                        }
                    });
            category.setName(name);
        }

        if (description != null) {
            category.setDescription(description);
        }

        if (displayOrder != null) {
            category.setDisplayOrder(displayOrder);
        }

        if (active != null) {
            category.setActive(active);
        }

        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);

        List<Category> subcategories = categoryRepository.findByParentCategoryId(id);
        if (!subcategories.isEmpty()) {
            throw new BusinessException("Cannot delete category with subcategories. Delete subcategories first.");
        }

        if (!category.getProducts().isEmpty()) {
            throw new BusinessException("Cannot delete category with products. Remove or reassign products first.");
        }

        category.setActive(false);
    }

    @Transactional
    public void moveCategory(Long categoryId, Long newParentId) {
        Category category = findById(categoryId);

        if (newParentId == null) {
            category.setParentCategory(null);
        } else {
            Category newParent = findById(newParentId);

            if (isDescendant(newParent, categoryId)) {
                throw new BusinessException("Cannot move category to its own descendant");
            }

            category.setParentCategory(newParent);
        }
    }

    // Helper methods

    private boolean isDescendant(Category potentialDescendant, Long ancestorId) {
        if (potentialDescendant == null) {
            return false;
        }
        if (potentialDescendant.getId().equals(ancestorId)) {
            return true;
        }

        return isDescendant(potentialDescendant.getParentCategory(), ancestorId);
    }

    public String getCategoryPath(Long categoryId) {
        Category category = findById(categoryId);
        return category.getFullPath();
    }
}
