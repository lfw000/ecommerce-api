package com.spring.luispa.ecommerce_api.domain.order;

import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "order_items")
public class OrderItem extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Positive
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "TEXT")
    private String productSnapshot;

    @Column(name = "discount_percentage")
    private Integer discountPercentage = 0;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @Positive
    @Column(name = "final_price", nullable = false, precision =  10, scale = 2)
    private BigDecimal finalPrice;

    protected OrderItem() {
        // No-args constructor
    }

    private OrderItem(Builder builder) {
        this.product = builder.product;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
        this.subtotal = builder.subtotal;
        this.productSnapshot = builder.productSnapshot;
        this.discountPercentage = builder.discountPercentage;
        this.discountAmount = builder.discountAmount;
        this.finalPrice = builder.finalPrice;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductSnapshot() {
        return productSnapshot;
    }

    public void setProductSnapshot(String productSnapshot) {
        this.productSnapshot = productSnapshot;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }

    // Domain methods

    public BigDecimal calculateRefundAmount(Integer quantityToRefund) {
        if (quantityToRefund == null || quantityToRefund <= 0 || quantityToRefund > quantity) {
            throw new IllegalArgumentException("Invalid refund quantity");
        }

        BigDecimal unitFinalPrice = finalPrice.divide(BigDecimal.valueOf(quantityToRefund), 2, RoundingMode.HALF_UP);

        return unitFinalPrice.multiply(BigDecimal.valueOf(quantityToRefund));
    }

    public boolean isSameProduct(Product product) {
        return this.product.equals(product);
    }

    public boolean isSameProductBySku(String sku) {
        return productSnapshot != null && productSnapshot.contains("\"sku\":\"" + sku + "\"");
    }

    // Builder

    public static Builder builder(Product product, Integer quantity) {
        return new Builder(product, quantity);
    }

    public static class Builder {
        // Required fields
        private final Product product;
        private final Integer quantity;

        // Optional fields
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String productSnapshot;
        private Integer discountPercentage = 0;
        private BigDecimal discountAmount = BigDecimal.ZERO;
        private BigDecimal finalPrice;

        public Builder(Product product, Integer quantity) {
            Objects.requireNonNull(product, "Product cannot be null");

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            this.product = product;
            this.quantity = quantity;
            this.unitPrice = product.getPrice();
            this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            this.finalPrice = this.subtotal;
            this.productSnapshot = createProductSnapshot(product);
        }

        private String createProductSnapshot(Product product) {
            System.out.println("Generando snapshot");

            return String.format(
                    "{\"sku\":\"%s\",\"name\":\"%s\",\"description\":\"%s\",\"category\":\"%s\"}",
                    product.getSku(),
                    product.getName(),
                    product.getDescription() != null ? product.getDescription() : "",
                    product.getCategory() != null ?  product.getCategory().getName() : ""
            );
        }

        public Builder withDiscount(Integer discountPercentage) {
            if (discountPercentage == null || discountPercentage < 0 || discountPercentage > 100) {
                throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
            }

            this.discountPercentage = discountPercentage;

            BigDecimal discountMultiplier = BigDecimal.valueOf(discountPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            this.discountAmount = this.subtotal.multiply(discountMultiplier);
            this.finalPrice = this.subtotal.subtract(this.discountAmount);

            return this;
        }

        public Builder unitPrice(BigDecimal unitPrice) {
            Objects.requireNonNull(unitPrice, "Unit price cannot be null");
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
            this.unitPrice = unitPrice;
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
            this.finalPrice = this.subtotal.subtract(this.discountAmount);
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem other = (OrderItem) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
