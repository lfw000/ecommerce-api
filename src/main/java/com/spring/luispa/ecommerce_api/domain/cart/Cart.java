package com.spring.luispa.ecommerce_api.domain.cart;

import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity(name = "Cart")
@Table(name = "carts")
public class Cart extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "converted_to_order")
    private Boolean convertedToOrder = false;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    protected Cart() {
        // No-args constructor
    }

    public Cart(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        this.user = user;
        this.expiresAt = calculateExpirationDate();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getConvertedToOrder() {
        return convertedToOrder;
    }

    public void setConvertedToOrder(Boolean convertedToOrder) {
        this.convertedToOrder = convertedToOrder;
    }

    public LocalDateTime getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(LocalDateTime convertedAt) {
        this.convertedAt = convertedAt;
    }

    // Domain methods

    private LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusDays(30);
    }

    public void addItem(Product product, Integer quantity) {
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");

        if (!active) {
            throw new IllegalStateException("Cannot add items to an inactive cart");
        }

        if (convertedToOrder) {
            throw new IllegalStateException("Cannot add items to a cart that has been converted to an order");
        }

        if (quantity <= 0) {
            throw new IllegalStateException("Quantity must be positive");
        }

        CartItem existingItem = findItemByProduct(product);

        if (existingItem != null) {
            existingItem.increaseQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(this, product, quantity);
            items.add(newItem);
        }

        this.expiresAt = calculateExpirationDate();
    }

    public void updateItemQuantity(Product product, Integer newQuantity) {
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(newQuantity, "Quantity cannot be null");

        CartItem item = findItemByProduct(product);
        if (item == null) {
            throw new IllegalStateException("Product not found in cart: " + product.getSku());
        }

        if (newQuantity <=  0) {
            removeItem(product);
        } else {
            item.updateQuantity(newQuantity);
        }

        this.expiresAt = calculateExpirationDate();
    }

    public void removeItem(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        CartItem item = findItemByProduct(product);
        if (item != null) {
            items.remove(item);
            item.setCart(null);
        }

        this.expiresAt = calculateExpirationDate();
    }

    public void clear() {
        items.clear();
        this.expiresAt = calculateExpirationDate();
    }

    public void markAsConverted() {
        this.active = false;
        this.convertedToOrder = true;
        this.convertedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    private CartItem findItemByProduct(Product product) {
        return items.stream()
                .filter(item -> item.hasSameProduct(product))
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this  == o) return true;
        if (!(o instanceof Cart)) return false;
        Cart other = (Cart) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
