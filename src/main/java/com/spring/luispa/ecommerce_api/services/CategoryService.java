package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.CategoryResponse;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.mappers.CategoryMapper;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return categoryMapper.toResponse(category);
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

    public List<CategoryResponse> findAllActiveOrdered() {
        List<Category> categories = categoryRepository.findAllActiveOrdered();

        return categoryMapper.toResponseList(categories);
    }

    public List<CategoryResponse> findRootCategories() {
        List<Category> categories = categoryRepository.findByParentCategoryIsNull();

        return  categoryMapper.toResponseList(categories);
    }

    public List<CategoryResponse> findSubcategories(Long parentId) {
        findById(parentId);

        List<Category> categories = categoryRepository.findByParentCategoryId(parentId);

        return categoryMapper.toResponseList(categories);
    }

    public String getCategoryPath(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return category.getFullPath();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessRuleException("Category already exists with name: " + request.getName());
        }

        Category category = new Category(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParentCategory(parent);
        }

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            categoryRepository.findByName(request.getName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessRuleException("Category name already used: " + request.getName());
                        }
                    });
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Category> subcategories = categoryRepository.findByParentCategoryId(id);
        if (!subcategories.isEmpty()) {
            throw new BusinessRuleException("Cannot delete category with subcategories. Delete subcategories first.");
        }

        if (!category.getProducts().isEmpty()) {
            throw new BusinessRuleException("Cannot delete category with products. Remove or reassign products first.");
        }

        category.setActive(false);
    }

    @Transactional
    public void moveCategory(Long categoryId, Long newParentId) {
        Category category = findCategoryEntity(categoryId);

        if (newParentId == null) {
            category.setParentCategory(null);
        } else {
            Category newParent = findCategoryEntity(newParentId);

            if (isDescendant(newParent, categoryId)) {
                throw new BusinessRuleException("Cannot move category to its own descendant");
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

    private Category findCategoryEntity(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }
}
