package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.product.Category;

public class CategoryTestHelper {

    // Methods for unit tests

    public static Category defaultCategory() {
        Category category = new Category("Electronics");
        category.setDescription("Electronic products");
        category.setId(1L);
        category.setActive(true);
        category.setDisplayOrder(1);
        return category;
    }

    public static Category defaultCategory(Long id) {
        Category category = defaultCategory();
        category.setId(id);
        return category;
    }

    public static Category categoryWithName(String name) {
        Category category = new Category(name);
        category.setId(1L);
        category.setActive(true);
        return category;
    }

    public static Category inactiveCategory() {
        Category category = new Category("Inactive");
        category.setId(2L);
        category.setActive(false);
        return category;
    }

    public static Category parentCategory() {
        Category category = new Category("Computers");
        category.setId(2L);
        category.setActive(true);
        return category;
    }

    public static Category childCategory(Category parent) {
        Category category = new Category("Laptops");
        category.setId(3L);
        category.setActive(true);
        category.setParentCategory(parent);
        return category;
    }

    // Methods for integration tests

    public static Category newCategory() {
        Category category = new Category("Electrónicos");
        category.setActive(true);
        category.setDisplayOrder(1);
        return category;
    }

    public static Category newCategory(String name) {
        Category category = new Category(name);
        category.setActive(true);
        category.setDisplayOrder(1);
        return category;
    }

    public static Category newCategory(String name, int displayOrder) {
        Category category = new Category(name);
        category.setActive(true);
        category.setDisplayOrder(displayOrder);
        return category;
    }

    public static Category newInactiveCategory(String name) {
        Category category = new Category(name);
        category.setActive(false);
        category.setDisplayOrder(1);
        return category;
    }

    public static Category newParentCategory() {
        Category category = new Category("Computadoras");
        category.setActive(true);
        category.setDisplayOrder(0);
        return category;
    }

    public static Category newChildCategory(Category parent) {
        Category category = new Category("Laptops");
        category.setActive(true);
        category.setDisplayOrder(1);
        category.setParentCategory(parent);
        return category;
    }
}
