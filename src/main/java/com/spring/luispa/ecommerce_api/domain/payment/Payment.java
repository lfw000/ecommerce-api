package com.spring.luispa.ecommerce_api.domain.payment;

import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.shared.common.Auditable;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false, length = 25)
    private String paymentNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

     @Column(length = 3, nullable = false)
    private String currency = "USD";

     @Column(name = "transaction_id", length = 100)
     private String transactionId;

     @Column(name = "payment_details", columnDefinition = "TEXT")
     private String paymentDetails;

     @Column(name = "paid_at")
     private LocalDateTime paidAt;

     @Column(name = "failed_at")
     private LocalDateTime failedAt;

     @Column(name = "failure_reason", length = 500)
     private String failureReason;

     @Column(name = "refunded_at")
     private LocalDateTime refundedAt;

     @Column(name = "refund_amount", precision = 10, scale = 2)
     private BigDecimal refundAmount;

     @Column(name = "refund_reason", length = 500)
     private String refundReason;

     @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<RefundTransaction> refundTransactions = new ArrayList<>();

    protected Payment() {
        // No-args constructor
    }

    private Payment(Builder builder) {
        this.paymentNumber = builder.paymentNumber;
        this.order = builder.order;
        this.paymentMethod = builder.paymentMethod;
        this.status = builder.status;
        this.currency = builder.currency;
        this.transactionId = builder.transactionId;
        this.paymentDetails = builder.paymentDetails;
        this.amount = builder.amount;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public List<RefundTransaction> getRefundTransactions() {
        return refundTransactions;
    }

    public void setRefundTransactions(List<RefundTransaction> refundTransactions) {
        this.refundTransactions = refundTransactions;
    }

    // Domain methods

    public void complete(String transactionId, String paymentDetails) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Only pending payments can be completed");
        }

        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentDetails = paymentDetails;
        this.paidAt = LocalDateTime.now();

        this.order.confirmPayment(transactionId);
    }

    public void fail(String reason) {
        if (status == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Completed payments cannot be marked as failed");
        }

        this.status = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public void refund(String reason) {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalStateException("Payment already fulled refunded");
        }

        RefundTransaction refund = new RefundTransaction(
                this,
                amount,
                reason,
                RefundType.FULL
        );

        this.refundTransactions.add(refund);
        this.status = PaymentStatus.REFUNDED;
        this.refundAmount = amount;
        this.refundedAt = LocalDateTime.now();
        this.refundReason = reason;
    }

    public void partialRefund(BigDecimal refundAmount, String reason) {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Refund amount must be positive");
        }

        BigDecimal totalRefunded = calculateTotalRefunded();
        if (totalRefunded.add(refundAmount).compareTo(this.amount) > 0) {
            throw new IllegalStateException("Refund amount exceeds payment amount");
        }

        RefundTransaction refund = new RefundTransaction(
                this,
                refundAmount,
                reason,
                RefundType.PARTIAL
        );
    }

    private BigDecimal calculateTotalRefunded() {
        return refundTransactions.stream()
                .map(RefundTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isRefundable() {
        return status == PaymentStatus.COMPLETED &&
                (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0);
    }

    public BigDecimal getPendingAmount() {
        if (refundAmount == null) {
            return amount;
        }
        return amount.subtract(refundAmount);
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return getId() != null && getId().equals(payment.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // Builder

    public static Builder builder(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        return new Builder(order, paymentMethod, amount);
    }

    public static class Builder {
        // Required fields
        private final Order order;
        private final PaymentMethod paymentMethod;
        private final BigDecimal amount;
        // Optional fields
        private String paymentNumber = UUID.randomUUID().toString();
        private PaymentStatus status = PaymentStatus.PENDING;
        private String currency = "USD";
        private String transactionId;
        private String paymentDetails;

        public Builder(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
            Objects.requireNonNull(order, "Order cannot be null");
            Objects.requireNonNull(paymentMethod, "PaymentMethod cannot be null");
            Objects.requireNonNull(amount, "Amount cannot be null");

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }

            this.order = order;
            this.paymentMethod = paymentMethod;
            this.amount = amount;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder paymentDetails(String paymentDetails) {
            this.paymentDetails = paymentDetails;
            return this;
        }

        public Builder currency(String currency) {
            Objects.requireNonNull(currency, "Currency cannot be null");
            this.currency = currency;
            return this;
        }

        public Payment build() {
            Payment payment = new Payment(this);
            payment.order.setPayment(payment);
            return payment;
        }
    }
}
