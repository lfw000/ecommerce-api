package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.product.Category;

public class CategoryTestHelper {

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
}
