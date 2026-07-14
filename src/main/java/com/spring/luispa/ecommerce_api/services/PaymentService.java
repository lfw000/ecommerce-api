package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ProcessPaymentRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefundRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.payment.RefundTransaction;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.PaymentMapper;
import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final LoggingAspect loggingAspect;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, PaymentMapper paymentMapper, LoggingAspect loggingAspect) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentMapper = paymentMapper;
        this.loggingAspect = loggingAspect;
    }
    
    public PaymentResponse findById(Long id, Long userId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            log.warn("User attempted to access payment belonging to another user: paymentId={}, ownerId={}",
                    payment.getId(), payment.getOrder().getUser().getId());
            throw new UnauthorizedException("Payment does not belong to user");
        }

        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse findByOrderIdForUser(Long orderId, Long userId) {
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
        loggingAspect.setUserIdInMDC(userId);

        log.info("Processing payment: orderId={}, method={}", orderId, request.getPaymentMethod());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for payment: orderId={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        if (!order.getUser().getId().equals(userId)) {
            log.warn("User attempted to pay order belonging to another user: orderId={}, ownerId={}", orderId,
                    order.getUser().getId());
            throw new UnauthorizedException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot pay order: orderId={}, currentStatus={}", orderId, order.getStatus());
            throw new BusinessRuleException("Order cannot be paid. Current status: " + order.getStatus());
        }

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            log.warn("Payment already exists for order: orderId={}", orderId);
            throw new BusinessRuleException("Payment already exists for this order");
        }

        Payment payment = new Payment.Builder(order, request.getPaymentMethod(), order.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();

        String paymentDetails = buildPaymentDetails(request);

        String transactionId = simulatePaymentGateway(payment, request);

        payment.complete(transactionId, paymentDetails);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment processed successfully: paymentId={}, orderId={}, transactionId={}, amount={}",
                savedPayment.getId(), orderId, transactionId, payment.getAmount());

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
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request, Long userId, String userRole) {
        loggingAspect.setUserIdInMDC(userId);

        log.info("Processing refund: paymentId={}, role={}, amount={}", paymentId, userRole, request.getAmount());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for refund: paymentId={}", paymentId);
                    return new ResourceNotFoundException("Payment not found");
                });

        Order order = payment.getOrder();

        if (!"ADMIN".equals(userRole) && !order.getUser().getId().equals(userId)) {
            log.warn("User attempted to refund payment belonging to another user: paymentId={}, ownerId={}",
                    paymentId, order.getUser().getId());
            throw new UnauthorizedException("Cannot refund this payment");
        }

        if (!payment.isRefundable()) {
            log.warn("Payment cannot be refunded: paymentId={}, currentStatus={}", paymentId, payment.getStatus());
            throw new BusinessRuleException(String.format("Payment cannot be refunded. Current status: %s", payment.getStatus()));
        }

        if (order.getStatus() != OrderStatus.SHIPPED || order.getStatus() != OrderStatus.DELIVERED) {
            log.warn("cannot refund shipped/delivered order: orderId={}, status={}", order.getId(), order.getStatus());
            throw new BusinessRuleException("Cannot refund payment for shipped or delivered orders. Please process a return instead.");
        }

        PaymentResponse response;

        if (request.getAmount() == null || request.getAmount().compareTo(payment.getAmount()) == 0) {
            payment.refund(request.getReason());
            log.debug("Full refund processed: paymentId={}, amount={}", paymentId, payment.getAmount());
        } else {
            payment.partialRefund(request.getAmount(), request.getReason());
            log.debug("Partial refund processed: paymentId={}, amount={}", paymentId, request.getAmount());
        }

        response = paymentMapper.toResponse(payment);

        if (request.isCancelOrderAfterRefund()) {
            log.debug("Cancelling order after refund: orderId={}", order.getId());
            CancelOrderRequest cancelRequest = new CancelOrderRequest(
                    CancellationReason.ADMIN_CANCELLED,
                    "Order cancelled after refund: " + request.getReason()
            );
            order.cancel(cancelRequest, userId, userRole);
        }

        log.info("Refund processed: paymentId={}, orderId={}, amount={}, reason={}",
                paymentId, order.getId(), payment.getRefundAmount(), request.getReason());

        return response;
    }

    @Transactional
    public PaymentResponse partialRefundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.isRefundable()) {
            throw new BusinessRuleException("Payment cannot be refunded. Status: " + payment.getStatus());
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Refund amount must be positive");
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

    // Private methods

    private String buildPaymentDetails(ProcessPaymentRequest request) {
        StringBuilder details = new StringBuilder("{");
        details.append("\"method\":\"").append(request.getPaymentMethod()).append("\",");
        details.append("\"currency\":\"").append(request.getCurrency()).append("\"");

        if (request.getCardLastFour() != null) {
            details.append(",\"cardLastFour\":\"").append(request.getCardLastFour()).append("\"");
        }
        if (request.getCardBrand() != null) {
            details.append(",\"cardBrand\":\"").append(request.getCardBrand()).append("\"");
        }

        details.append("}");

        return details.toString();
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
