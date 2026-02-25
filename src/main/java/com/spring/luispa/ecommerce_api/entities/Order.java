package com.spring.luispa.ecommerce_api.entities;

import com.spring.luispa.ecommerce_api.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.enums.PaymentMethod;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @OneToOne(mappedBy = "order", cascade =  CascadeType.ALL)
    private Payment payment;

    protected Order() {
        // No-args constructor
    }

    private Order(Builder builder) {
        this.orderNumber = builder.orderNumber;
        this.user = builder.user;
        this.totalAmount = builder.totalAmount;
        this.status = builder.status;
        this.shippingAddress = builder.shippingAddress;
        this.paymentMethod = builder.paymentMethod;
        this.items = builder.items;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getOrderStatus() {
        return status;
    }

    public void setOrderStatus(OrderStatus status) {
        this.status = status;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    // Helper methods

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!isValidTransition(status, newStatus)) {
            throw new IllegalStateException("Invalid transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public static String generateOrderNumber() {
        return "ORDER-" + System.currentTimeMillis();
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        switch (current) {
            case PENDING -> {
                return next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            }
            case PAID -> {
                return next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            }
            // Includes DELIVERED, CANCELLED
            default -> {
                return false;
            }
        }
    }

    // equals() and hashCode()

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();

        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;

        Order order = (Order) o;
        return getId() != null && Objects.equals(getId(), order.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderNumber;
        private User user;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private Address shippingAddress;
        private PaymentMethod paymentMethod;
        private List<OrderItem> items = new ArrayList<>();

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public  Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder shippingAddress(Address shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder addItem(OrderItem item) {
            this.items.add(item);
            return this;
        }

        public Order build() {
            if (orderNumber == null || orderNumber.isEmpty()) {
                throw new IllegalArgumentException("Order number is required");
            }
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Total amount should be greater than zero");
            }
            if (status == null) {
                throw new IllegalArgumentException("Order status is required");
            }
            return new Order(this);
        }
    }
}
