package com.spring.luispa.ecommerce_api.domain.order;

import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.shared.common.Auditable;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 36)
    @Column(name = "order_number", nullable = false, unique = true, length = 36)
    private String orderNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade =  {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull
    @Size(max = 20)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @Positive
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id", nullable = false)
    private Address billingAddress;

    @Size(max = 50)
    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Size(max = 100)
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimateDeliveryDate;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Size(max = 50)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Size(max = 1000)
    @Column(length = 1000)
    private String notes;

    @OneToOne(mappedBy = "order", cascade =  {CascadeType.PERSIST, CascadeType.MERGE})
    private Payment payment;

    protected Order() {
        // No-args constructor
    }

    private Order(Builder builder) {
        this.orderNumber = builder.orderNumber;
        this.user = builder.user;
        this.status = builder.status;
        this.subtotal = builder.subtotal;
        this.shippingCost = builder.shippingCost;
        this.taxAmount = builder.taxAmount;
        this.totalAmount = builder.totalAmount;
        this.shippingAddress = builder.shippingAddress;
        this.billingAddress = builder.billingAddress;
        this.shippingMethod = builder.shippingMethod;
        this.estimateDeliveryDate = builder.estimateDeliveryDate;
        this.notes = builder.notes;

        // Set items
        this.items = new ArrayList<>(builder.items);
        this.items.forEach(item -> item.setOrder(this));
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getEstimateDeliveryDate() {
        return estimateDeliveryDate;
    }

    public void setEstimateDeliveryDate(LocalDateTime estimateDeliveryDate) {
        this.estimateDeliveryDate = estimateDeliveryDate;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    // Domain methods

    public void confirmPayment(String transactionId) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        status = OrderStatus.PAID;
    }

    public void ship(String trackingNumber) {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only paid orders can be shipped");
        }
        status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
    }

    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders  can be delivered");
        }
        status = OrderStatus.DELIVERED;
        deliveredAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivery orders cannot be cancelled");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        status = OrderStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
        cancellationReason = reason;
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.PAID;
    }

    private Integer getItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    public OrderSummary getSummary() {
        return new OrderSummary(
                orderNumber,
                status,
                totalAmount,
                getItemCount(),
                getCreatedAt(),
                estimateDeliveryDate
        );
    }

    // equals() and hashCode()

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return getId() != null && getId().equals(order.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder(User user, Address shippingAddress, Address billingAddress,
                                  Set<OrderItem> items) {
        return new Builder(user, shippingAddress, billingAddress, items);
    }

    public static class Builder {
        // Required fields
        private final User user;
        private final Address shippingAddress;
        private final Address billingAddress;
        private final List<OrderItem> items;

        // Optional fields
        private String orderNumber = UUID.randomUUID().toString();
        private OrderStatus status =  OrderStatus.PENDING;
        private BigDecimal subtotal;
        private BigDecimal shippingCost = BigDecimal.ZERO;
        private BigDecimal taxAmount = BigDecimal.ZERO;
        private BigDecimal totalAmount;
        private String shippingMethod;
        private LocalDateTime estimateDeliveryDate;
        private String notes;

        public Builder(User user, Address shippingAddress, Address billingAddress, Set<OrderItem> items) {
            Objects.requireNonNull(user, "User cannot be null");
            Objects.requireNonNull(shippingAddress, "Shipping address cannot be null");
            Objects.requireNonNull(billingAddress, "Billing address cannot be null");
            Objects.requireNonNull(items, "Items cannot be null");

            if (items.isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one item");
            }

            this.user = user;
            this.shippingAddress = shippingAddress;
            this.billingAddress = billingAddress;
            this.items = new ArrayList<>(items);

            subtotal = items.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.totalAmount = subtotal;
        }

        public Builder shippingCost(BigDecimal shippingCost) {
            Objects.requireNonNull(shippingCost, "Shipping cost cannot be null");
            if (shippingCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Shipping cost cannot be negative");
            }
            this.shippingCost = shippingCost;
            recalculateTotal();
            return this;
        }

        public Builder taxAmount(BigDecimal taxAmount) {
            Objects.requireNonNull(taxAmount, "Tax amount cannot be null");
            if (taxAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Tax amount cannot be negative");
            }
            this.taxAmount = taxAmount;
            recalculateTotal();
            return this;
        }

        public Builder shippingMethod(String shippingMethod) {
            this.shippingMethod = shippingMethod;
            return this;
        }

        public Builder estimateDeliveryDate(LocalDateTime estimateDeliveryDate) {
            this.estimateDeliveryDate = estimateDeliveryDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        private void recalculateTotal() {
            this.totalAmount = subtotal.add(shippingCost).add(taxAmount);
        }

        public Order build() {
            return new Order(this);
        }
    }

    public record OrderSummary(
            String orderNumber,
            OrderStatus status,
            BigDecimal total,
            Integer itemCount,
            LocalDateTime createdAt,
            LocalDateTime estimateDeliveryDate
    ){}
}
