package com.spring.luispa.ecommerce_api.api.dto.response;

import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order information response")
public class OrderResponse {

    @Schema(description = "Order ID",
            example = "1")
    private Long id;

    @Schema(description = "Unique order number (public identifier)",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderNumber;

    @Schema(description = "User ID",
            example = "1")
    private Long userId;

    @Schema(description = "User email",
            example = "usuario@example.com")
    private String userEmail;

    @Schema(description = "Order items")
    private List<OrderItemResponse> items;

    @Schema(description = "Order status",
            example = "PAID")
    private OrderStatus status;

    @Schema(description = "Subtotal (without shipping/taxes)",
            example = "1299.98")
    private BigDecimal subtotal;

    @Schema(description = "Shipping cost",
            example = "10.00")
    private BigDecimal shippingCost;

    @Schema(description = "Tax amount",
            example = "130.00")
    private BigDecimal taxAmount;

    @Schema(description = "Total amount (subtotal + shipping + tax)",
            example = "1439.98")
    private BigDecimal totalAmount;

    @Schema(description = "Shipping address")
    private AddressResponse shippingAddress;

    @Schema(description = "Billing Address")
    private AddressResponse billingAddress;

    @Schema(description = "Shipping method",
            example = "standard")
    private String shippingMethod;

    @Schema(description = "Tracking number",
            example = "1Z999AA10123456784")
    private String trackingNumber;

    @Schema(description = "Estimated delivery date",
            example = "2024-01-25T10:30:00")
    private LocalDateTime estimatedDeliveryDate;

    @Schema(description = "Actual delivery date",
            example = "2024-01-23T14:30:00")
    private LocalDateTime deliveredAt;

    @Schema(description = "Creation date",
            example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Payment information")
    private PaymentResponse payment;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
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

    public AddressResponse getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressResponse shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public AddressResponse getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(AddressResponse billingAddress) {
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
        return estimatedDeliveryDate;
    }

    public void setEstimateDeliveryDate(LocalDateTime estimateDeliveryDate) {
        this.estimatedDeliveryDate = estimateDeliveryDate;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PaymentResponse getPayment() {
        return payment;
    }

    public void setPayment(PaymentResponse payment) {
        this.payment = payment;
    }
}
