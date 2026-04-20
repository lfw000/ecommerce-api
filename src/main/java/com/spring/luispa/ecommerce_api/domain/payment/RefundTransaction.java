package com.spring.luispa.ecommerce_api.domain.payment;

import com.spring.luispa.ecommerce_api.shared.common.AuditableBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refund_transactions")
public class RefundTransaction extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @NotNull
    @Column(name = "refund_number", nullable = false, unique = true, length = 36)
    private String refundNumber;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Column(length = 500)
    private String reason;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    protected RefundTransaction() {
        // No-args constructor
    }

    public RefundTransaction(Payment payment, BigDecimal amount, String reason, RefundType refundType) {
        Objects.requireNonNull(payment, "Payment cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(refundType, "Refund type cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }

        this.payment = payment;
        this.refundNumber = UUID.randomUUID().toString();
        this.amount = amount;
        this.reason = reason;
        this.refundType = refundType;
        this.refundedAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getRefundNumber() {
        return refundNumber;
    }

    public void setRefundNumber(String refundNumber) {
        this.refundNumber = refundNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    // equals() and hashCode()

    @Override
    public boolean equals(Object o) {
        if (this == o)  return true;
        if (!(o instanceof RefundTransaction)) return false;
        RefundTransaction other = (RefundTransaction) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
