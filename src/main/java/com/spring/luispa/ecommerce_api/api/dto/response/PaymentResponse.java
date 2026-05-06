package com.spring.luispa.ecommerce_api.api.dto.response;

import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Payment information response")
public class PaymentResponse {

    @Schema(description = "Payment ID",
            example = "1")
    private Long id;

    @Schema(description = "Payment number (public identifier)",
        example = "660e8400-e29b-41d4-a716-446655440001")
    private String paymentNumber;

    @Schema(description = "Order ID",
            example = "1")
    private Long orderId;

    @Schema(description = "Payment method",
            example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment status",
            example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "Payment amount",
            example = "1439.98")
    private BigDecimal amount;

    @Schema(description = "Currency",
            example = "USD")
    private String currency;

    @Schema(description = "Transaction ID from gateway",
        example = "tx_1234567890abcdef")
    private String transactionId;

    @Schema(description = "Payment date",
            example = "2024-01-15T10:30:00")
    private LocalDateTime paidAt;

    @Schema(description = "Refund date",
            example = "2024-01-20T14:25:00")
    private LocalDateTime refundedAt;

    @Schema(description = "Refund amount",
            example = "1439.98")
    private BigDecimal refundAmount;

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

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
}
