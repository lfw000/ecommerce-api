package com.spring.luispa.ecommerce_api.services.validation;

import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentValidator {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentValidator(OrderRepository orderRepository,
                            PaymentRepository paymentRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public Order validateOrderForPayment(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        return order;
    }

    public void validateOrderStatus(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException("Order cannot be paid. Current status: " + order.getStatus());
        }
    }

    public void validateNoExistingPayment(Long orderId) {
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessRuleException("Payment already exists for this order");
        }
    }

    public void validatePaymentExists(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new ResourceNotFoundException("Payment not found with id: " + paymentId);
        }
    }

    public Payment validatePaymentForRefund(Long paymentId, Long userId, String userRole) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Order order = payment.getOrder();

        if (!"ADMIN".equals(userRole) && !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot refund this payment");
        }

        if (!payment.isRefundable()) {
            throw new BusinessRuleException(
                    String.format("Payment cannot be refunded. Current status: %s", payment.getStatus())
            );
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    "Cannot refund payment for shipped or delivered orders. Please process a return instead."
            );
        }

        return payment;
    }

    public void validateRefundAmount(BigDecimal refundAmount, Payment payment) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Refund amount must be positive");
        }

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessRuleException("Refund amount exceeds payment amount");
        }
    }

}
