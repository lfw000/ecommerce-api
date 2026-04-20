package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.ProcessPaymentRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefundRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.payment.RefundTransaction;
import com.spring.luispa.ecommerce_api.mappers.PaymentMapper;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
    }
    
    public PaymentResponse findById(Long id, Long userId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Payment does not belong to user");
        }

        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse findByOrderId(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Payment does not belong to user");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> findPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);

        return paymentMapper.toResponseList(payments);
    }

    public List<PaymentResponse> findByStatus(PaymentStatus status) {
        List<Payment> payments = paymentRepository.findByStatus(status);

        return paymentMapper.toResponseList(payments);
    }

    public PaymentResponse findByPaymentNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with number: " + paymentNumber));

        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(Long orderId, ProcessPaymentRequest request, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        if (!order.isCancellable()) {
            throw new BusinessException("Order cannot be paid in current status: " + order.getStatus());
        }

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("Payment already exists for this order");
        }

        Payment payment = new Payment.Builder(order, request.getPaymentMethod(), order.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();

        String transactionId = simulatePaymentGateway(payment, request);

        payment.complete(transactionId, buildPaymentDetails(request));

        Payment savedPayment = paymentRepository.save(payment);

        order.confirmPayment(transactionId);

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse failPayment(Long paymentId, String reason, Long userId, boolean isAdmin) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!isAdmin && !payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Payment does not belong to user");
        }

        payment.fail(reason);

        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.isRefundable()) {
            throw new BusinessException("Payment cannot be refunded. Status: " + payment.getStatus());
        }

        payment.refund(request.getReason());

        Payment savedPayment = paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.cancel(request.getReason());

        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse partialRefundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.isRefundable()) {
            throw new BusinessException("Payment cannot be refunded. Status: " + payment.getStatus());
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Refund amount must be positive");
        }

        payment.partialRefund(request.getAmount(), request.getReason());

        Payment savedPayment = paymentRepository.save(payment);

        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse findByIdForAdmin(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> findAllForAdmin() {
        return paymentMapper.toResponseList(paymentRepository.findAll());
    }

    public List<RefundTransaction> getRefundHistory(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        return payment.getRefundTransactions();
    }

    private String simulatePaymentGateway(Payment payment, ProcessPaymentRequest request) {
        return "tx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String buildPaymentDetails(ProcessPaymentRequest request) {
        // En un caso real, almacenar información como:
        // - Últimos 4 dígitos de la tarjeta
        // - Marca de la tarjeta (Visa, Mastercard)
        // - ID del cliente en la pasarela
        return String.format("{\"method\":\"%s\", \"currency\":\"%s\"}",
                request.getPaymentMethod().name(),
                request.getCurrency() != null ? request.getCurrency() : "USD");
    }

    @Transactional
    public void cancelExpiredPendingPayments() {
        LocalDateTime expirationDate = LocalDateTime.now().minusMinutes(30);
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsOlderThan(
                PaymentStatus.PENDING, expirationDate);

        for (Payment payment : pendingPayments) {
            payment.fail("Payment expired");
        }

        paymentRepository.saveAll(pendingPayments);
    }
}
