package com.spring.luispa.ecommerce_api.domain.cart;

import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "CartItem")
@Table(name = "cart_items")
public class CartItem extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id",  nullable = false)
    private Cart cart;

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
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    protected CartItem() {
        // No-args constructor
    }

    public CartItem(Cart cart, Product product, Integer quantity) {
        Objects.requireNonNull(cart, "Cart cannot be null");
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!product.hasStock(quantity)) {
            throw new IllegalStateException(String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                    product.getSku(),
                    product.getStock(),
                    quantity));
        }

        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.subtotal = calculateSubtotal();
    }

    public CartItem(Cart cart, Product product, Integer quantity, BigDecimal price) {
        this(cart, product, quantity);
        Objects.requireNonNull(price, "Price cannot be null");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        this.price = price;
        this.subtotal = calculateSubtotal();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal unitPrice) {
        this.price = unitPrice;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Domain methods

    public void updateQuantity(Integer newQuantity) {
        Objects.requireNonNull(newQuantity, "Quantity cannot be null");

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!product.hasStock(newQuantity)) {
            throw new IllegalStateException(
                    String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                            product.getSku(),
                            product.getStock(),
                            newQuantity));
        }

        this.quantity = newQuantity;
        this.subtotal = calculateSubtotal();
    }

    public void increaseQuantity(Integer amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        updateQuantity(this.quantity + amount);
    }

    public void decreaseQuantity(Integer amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        int newQuantity = this.quantity - amount;
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity cannot be zero or negative");
        }

        updateQuantity(newQuantity);
    }

    public boolean hasSameProduct(Product product) {
        return this.product.equals(product);
    }

    private BigDecimal calculateSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this  == o) return true;
        if (!(o instanceof CartItem)) return false;
        CartItem other = (CartItem) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
