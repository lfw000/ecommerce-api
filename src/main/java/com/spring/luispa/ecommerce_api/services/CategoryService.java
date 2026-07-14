package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CreateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.UpdateCategoryRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.CategoryResponse;
import com.spring.luispa.ecommerce_api.domain.product.Category;
import com.spring.luispa.ecommerce_api.domain.product.CategoryRepository;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.CategoryMapper;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final LoggingAspect loggingAspect;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper, LoggingAspect loggingAspect) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.loggingAspect = loggingAspect;
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
        log.info("Creating category: name={}, parentId={}", request.getName(), request.getParentId());

        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category already exists: name={}", request.getName());
            throw new BusinessRuleException("Category already exists with name: " + request.getName());
        }

        Category category = new Category(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParentCategory(parent);
            log.debug("Category created under parent: parentId={}, parentName={}",
                    parent.getId(),  parent.getName());
        }

        log.info("Creating category: categoryId={}, name={}, parentId={}",
                category.getId(), category.getName(), request.getParentId());

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        log.info("Updating category: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for update: categoryId={}", id);
                    return new ResourceNotFoundException("Category not found");
                });

        if (request.getName() != null && !request.getName().isBlank()) {
            categoryRepository.findByName(request.getName())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            log.warn("Category name already used: name={}, existingId={}",
                                    request.getName(), existing.getId());
                            throw new BusinessRuleException("Category name already used: " + request.getName());
                        }
                    });
            category.setName(request.getName());
        }

        String oldName = category.getName();
        Integer oldDisplayOrder = category.getDisplayOrder();
        Boolean oldActive = category.isActive();

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        log.info("Category updated: categoryId={}, name= {}->{}, displayOrder={}->{}, active: {}->{}",
                category.getId(), oldName, category.getName(), oldDisplayOrder, category.getDisplayOrder(), oldActive,
                category.isActive());

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category: id={}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found for deletion: categoryId={}", id);
                    return new ResourceNotFoundException("Category not found");
                });

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
        log.info("Moving category:  categoryId={}, newParentId={}", categoryId, newParentId);

        Category category = findCategoryEntity(categoryId);
        Long oldParentId = category.getParentCategory() != null ? category.getParentCategory().getId()
                : null;

        if (newParentId == null) {
            category.setParentCategory(null);
            log.debug("Category moved to root: categoryId={}, oldParentId={}", categoryId, oldParentId);
        } else {
            Category newParent = findCategoryEntity(newParentId);

            if (isDescendant(newParent, categoryId)) {
                log.warn("Cannot move category to its own descendant: categoryId={}, newParentId={}",
                        categoryId, newParentId);
                throw new BusinessRuleException("Cannot move category to its own descendant");
            }

            category.setParentCategory(newParent);
            log.debug("Category moved under parent: categoryId={}, oldParentId={}, newParentId={}",
                    categoryId, oldParentId, newParentId);
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
