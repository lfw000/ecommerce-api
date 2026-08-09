package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.ProcessPaymentRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefundRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.payment.RefundTransaction;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.PaymentMapper;
import com.spring.luispa.ecommerce_api.services.validation.PaymentValidator;
import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedAccessException;
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
    private final PaymentMapper paymentMapper;
    private final PaymentValidator paymentValidator;

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final LoggingAspect loggingAspect;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentMapper paymentMapper,
                          PaymentValidator paymentValidator,
                          LoggingAspect loggingAspect) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentValidator = paymentValidator;
        this.loggingAspect = loggingAspect;
    }
    
    public PaymentResponse findById(Long id, Long userId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            log.warn("User attempted to access payment belonging to another user: paymentId={}, ownerId={}",
                    payment.getId(), payment.getOrder().getUser().getId());
            throw new UnauthorizedAccessException("Payment does not belong to user");
        }

        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse findByOrderIdForUser(Long orderId, Long userId) {
        paymentValidator.validateOrderForPayment(orderId, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        return paymentMapper.toResponse(payment);
    }

    public List<PaymentResponse> findPaymentsByUserId(Long userId) {
        log.info("Finding payments for uer: {}", userId);
        paymentValidator.validateUser(userId);
        List<Payment> payments = paymentRepository.findPaymentsByUserId(userId);
        log.info("Found {} payments", payments.size());
        if (!payments.isEmpty()) {
            log.debug("First payment: id={}, orderId={}", payments.get(0).getId(), payments.get(0).getOrder().getId());
        }

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

        paymentValidator.validateUser(userId);
        Order order = paymentValidator.validateOrderForPayment(orderId, userId);
        paymentValidator.validateOrderStatus(order);
        paymentValidator.validateNoExistingPayment(orderId);

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
            throw new UnauthorizedAccessException("Payment does not belong to user");
        }

        payment.fail(reason);

        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request, Long userId, String userRole) {
        loggingAspect.setUserIdInMDC(userId);

        log.info("Processing refund: paymentId={}, role={}, amount={}", paymentId, userRole, request.getAmount());

        paymentValidator.validateUser(userId);
        Payment payment = paymentValidator.validatePaymentForRefund(paymentId, userId, userRole);

        // ✅ Solo UNA vez se hace el refund
        if (request.getAmount() == null || request.getAmount().compareTo(payment.getAmount()) == 0) {
            payment.refund(request.getReason());
            log.debug("Full refund processed: paymentId={}, amount={}", paymentId, payment.getAmount());
        } else {
            paymentValidator.validateRefundAmount(request.getAmount(), payment);
            payment.partialRefund(request.getAmount(), request.getReason());
            log.debug("Partial refund processed: paymentId={}, amount={}", paymentId, request.getAmount());
        }

        Order order = payment.getOrder();
        Payment savedPayment = paymentRepository.save(payment);

        if (request.isCancelOrderAfterRefund()) {
            log.debug("Cancelling order after refund: orderId={}", order.getId());
            CancelOrderRequest cancelRequest = new CancelOrderRequest(
                    CancellationReason.ADMIN_CANCELLED,
                    "Order cancelled after refund: " + request.getReason()
            );
            order.cancel(cancelRequest, userId, userRole);
        }

        log.info("Refund processed: paymentId={}, orderId={}, amount={}, reason={}",
                paymentId, payment.getOrder().getId(), payment.getRefundAmount(), request.getReason());

        return paymentMapper.toResponse(savedPayment);
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

        if (pendingPayments.isEmpty()) {
            log.debug("No expired pending payments found");
            return;
        }

        for (Payment payment : pendingPayments) {
            payment.fail("Payment expired");
        }

        paymentRepository.saveAll(pendingPayments);
    }
}
