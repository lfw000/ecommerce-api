package com.spring.luispa.ecommerce_api.entities;

import com.spring.luispa.ecommerce_api.enums.PaymentMethod;
import com.spring.luispa.ecommerce_api.enums.PaymentStatus;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "payments")
public class Payment extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "transaction_id")
    private String transactionId;

    public Payment() {
        // No-args constructor
    }

    public Payment(Order order, BigDecimal amount, PaymentStatus status, PaymentMethod method) {
        this.order = order;
        this.amount = amount;
        this.status = status;
        this.method = method;
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
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // Helper methods

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
        if (newStatus == PaymentStatus.COMPLETED && paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    @PrePersist
    protected void onCreate() {
        if (status == PaymentStatus.COMPLETED && paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    // equals() and hashCode()

    @Override
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

        Payment payment = (Payment) o;
        return getId() != null && Objects.equals(getId(), payment.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
