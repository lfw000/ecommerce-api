package com.spring.luispa.ecommerce_api.domain.product;

import com.spring.luispa.ecommerce_api.shared.common.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "products")
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotNull
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private boolean featured;

    @Convert(converter = ProductAttributesConverter.class)
    @Column(columnDefinition = "TEXT")
    private ProductAttributes attributes = new ProductAttributes();

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(precision = 10, scale = 2)
    private BigDecimal length;

    @Column(precision = 10, scale = 2)
    private BigDecimal width;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public Product() {
        // No-args constructor
    }

    private Product(Builder builder) {
        this.sku = builder.sku;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.stock = builder.stock;
        this.category = builder.category;
        this.active = builder.active;
        this.featured = builder.featured;
        this.attributes = builder.attributes;
        this.weight = builder.weight;
        this.length = builder.length;
        this.width = builder.width;
        this.height = builder.height;
        this.lowStockThreshold = builder.lowStockThreshold;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public ProductAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ProductAttributes attributes) {
        this.attributes = attributes;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    // Domain methods

    public boolean hasStock(Integer quantity) {
        return stock >= quantity;
    }

    public void decreaseStock(Integer quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for product " + getSku());
        }
        this.stock -= quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }

    public void updateStock(Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new IllegalStateException("Stock cannot be negative");
        }

        this.stock = newStock;
    }

    public void adjust(Integer delta) {
        if (delta == null || (stock + delta) < 0) {
            throw new IllegalStateException("Stock cannot be negative");
        }

        this.stock += delta;
    }

    public boolean isLowStock() {
        return stock <= lowStockThreshold;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Price must be positive");
        }
        this.price = newPrice;
    }

    // Images management

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void setMainImage(ProductImage image) {
        images.forEach(img -> img.setMain(img.equals(image)));
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product other = (Product) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder(String sku, String name, BigDecimal price, Category category) {
        return new Builder(sku, name, price, category);
    }

    public static class Builder {
        // Required fields
        private String sku;
        private String name;
        private BigDecimal price;
        private Category category;

        // Optional with default values
        private String description;
        private Integer stock = 0;
        private Boolean active = true;
        private Boolean featured = false;
        private ProductAttributes attributes;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private Integer lowStockThreshold = 5;

        public Builder(String sku, String name, BigDecimal price, Category category) {
            if (sku == null) {
                throw new IllegalArgumentException("SKU cannot be null");
            } else if (name == null) {
                throw new IllegalArgumentException("Name cannot be null");
            } else if (price == null) {
                throw new IllegalArgumentException("Price cannot be null");
            } else if (category == null) {
                throw new IllegalArgumentException("Category cannot be null");
            }

            this.sku = sku;
            this.name = name;
            this.price = price;
            this.category = category;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder featured(Boolean featured) {
            this.featured = featured;
            return this;
        }

        public Builder attributes(ProductAttributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder weight(BigDecimal weight) {
            this.weight = weight;
            return this;
        }

        public Builder dimensions(BigDecimal length, BigDecimal width, BigDecimal height) {
            this.length = length;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder lowStockThreshold(Integer lowStockThreshold) {
            this.lowStockThreshold = lowStockThreshold;
            return this;
        }

        public Product build() {
            if (stock < 0) {
                throw new IllegalStateException("Stock cannot be negative");
            }
            if (lowStockThreshold < 0) {
                throw new IllegalStateException("Low stock threshold cannot be negative");
            }
            return new Product(this);
        }


    }
}
