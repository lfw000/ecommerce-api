package com.spring.luispa.ecommerce_api.domain.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
public class Category extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Category> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    public Category() {
        // No-args constructor
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and setters

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

    public Category getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(Category parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Category> subCategories) {
        this.subCategories = subCategories;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    // Domain methods

    public void addSubcategory(Category subcategory) {
        subCategories.add(subcategory);
        subcategory.setParentCategory(this);
    }

    public void removeSubcategory(Category subcategory) {
        subCategories.remove(subcategory);
        subcategory.setParentCategory(null);
    }

    public boolean isRootCategory() {
        return parentCategory == null;
    }

    public String getFullPath() {
        if (isRootCategory()) {
            return name;
        }

        return parentCategory.getFullPath() + " > " + name;
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this  == o) return true;
        if (!(o instanceof Category)) return false;
        Category other = (Category) o;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
