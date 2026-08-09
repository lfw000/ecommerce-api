package com.spring.luispa.ecommerce_api.unit.services;

import com.spring.luispa.ecommerce_api.api.dto.request.ProcessPaymentRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefundRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.payment.RefundTransaction;
import com.spring.luispa.ecommerce_api.domain.payment.RefundType;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.Role;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.PaymentMapper;
import com.spring.luispa.ecommerce_api.services.PaymentService;
import com.spring.luispa.ecommerce_api.services.validation.PaymentValidator;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentMethod;
import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedAccessException;
import com.spring.luispa.ecommerce_api.test.helpers.OrderTestHelper;
import com.spring.luispa.ecommerce_api.test.helpers.UserTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    // Mocks

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentValidator paymentValidator;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private LoggingAspect loggingAspect;

    private PaymentService paymentService;

    // Test data

    private User testUser;
    private Order testOrder;
    private Payment testPayment;
    private PaymentResponse testResponse;
    private ProcessPaymentRequest processRequest;
    private RefundRequest refundRequest;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                paymentMapper,
                paymentValidator,
                loggingAspect);

        testUser = UserTestHelper.defaultUser(1L);
        testOrder = OrderTestHelper.minimalOrder(1L);
        testOrder.setUser(testUser);

        Payment realPayment = new Payment.Builder(testOrder, PaymentMethod.CREDIT_CARD, new BigDecimal("150.00"))
                .build();
        realPayment.setId(1L);
        realPayment.setStatus(PaymentStatus.COMPLETED);
        testPayment = spy(realPayment);

        testResponse = new PaymentResponse();
        testResponse.setId(1L);
        testResponse.setOrderId(1L);
        testResponse.setStatus(PaymentStatus.COMPLETED);

        processRequest = new ProcessPaymentRequest();
        processRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        processRequest.setCurrency("USD");

        refundRequest = new RefundRequest();
        refundRequest.setReason("Customer requested");
        refundRequest.setAmount(new BigDecimal("150.00"));
    }

    // Process payment tests

    @Nested
    @DisplayName("Process Payment Tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should process payment when order is pending and user is owner")
        void shouldProcessPayment_whenOrderIsPending() {
            when(paymentValidator.validateOrderForPayment(1L, 1L)).thenReturn(testOrder);
            doNothing().when(paymentValidator).validateOrderStatus(testOrder);
            doNothing().when(paymentValidator).validateNoExistingPayment(1L);

            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.processPayment(1L, processRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowException_whenOrderNotFound() {
            when(paymentValidator.validateOrderForPayment(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

            assertThatThrownBy(() -> paymentService.processPayment(999L, processRequest, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when order does not belong to user")
        void shouldThrowException_whenOrderNotOwnedByUser() {
            when(paymentValidator.validateOrderForPayment(1L, 2L))
                    .thenThrow(new UnauthorizedAccessException("Order does not belong to user"));

            assertThatThrownBy(() -> paymentService.processPayment(1L, processRequest, 2L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("Should throw exception when order is not pending")
        void shouldThrowException_whenOrderNotPending() {
            when(paymentValidator.validateOrderForPayment(1L, 1L)).thenReturn(testOrder);
            doThrow(new BusinessRuleException("Order cannot be paid. Current status: PAID"))
                    .when(paymentValidator).validateOrderStatus(testOrder);

            assertThatThrownBy(() -> paymentService.processPayment(1L, processRequest, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Order cannot be paid");
        }

        @Test
        @DisplayName("Should throw exception when payment already exists")
        void shouldThrowException_whenPaymentAlreadyExists() {
            when(paymentValidator.validateOrderForPayment(1L, 1L)).thenReturn(testOrder);
            doNothing().when(paymentValidator).validateOrderStatus(testOrder);
            doThrow(new BusinessRuleException("Payment already exists for this order"))
                    .when(paymentValidator).validateNoExistingPayment(1L);

            assertThatThrownBy(() -> paymentService.processPayment(1L, processRequest, 1L))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Payment already exists");
        }
    }

    // Queries tests

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("Should return payment by ID when user is owner")
        void shouldReturnPaymentById_whenUserIsOwner() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.findById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when payment belongs to another user")
        void shouldThrowException_whenPaymentNotOwnedByUser() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

            assertThatThrownBy(() -> paymentService.findById(1L, 2L))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }

        @Test
        @DisplayName("Should return payment by order ID when user is owner")
        void shouldReturnPaymentByOrderId_whenUserIsOwner() {
            when(paymentValidator.validateOrderForPayment(1L, 1L)).thenReturn(testOrder);
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.findByOrderIdForUser(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return payments by user ID")
        void shouldReturnPaymentsByUserId() {
            when(paymentValidator.validateUser(1L)).thenReturn(testUser);
            when(paymentRepository.findPaymentsByUserId(1L)).thenReturn(List.of(testPayment));
            when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<PaymentResponse> results = paymentService.findPaymentsByUserId(1L);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should return payments by status")
        void shouldReturnPaymentsByStatus() {
            when(paymentRepository.findByStatus(PaymentStatus.COMPLETED)).thenReturn(List.of(testPayment));
            when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<PaymentResponse> results = paymentService.findByStatus(PaymentStatus.COMPLETED);

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should return payment by payment number")
        void shouldReturnPaymentByPaymentNumber() {
            when(paymentRepository.findByPaymentNumber("PAY-001")).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.findByPaymentNumber("PAY-001");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    // Refund tests

    @Nested
    @DisplayName("Refund Tests")
    class RefundTests {

        @Test
        @DisplayName("Should refund payment when user is admin")
        void shouldRefundPayment_whenAdmin() {
            testPayment.setStatus(PaymentStatus.COMPLETED);  // ← Esto es lo que faltaba

            when(paymentValidator.validatePaymentForRefund(1L, 1L, "ADMIN")).thenReturn(testPayment);
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.refundPayment(1L, refundRequest, 1L, "ADMIN");

            assertThat(result).isNotNull();
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should allow user to refund their own payment")
        void shouldAllowUserToRefundTheirOwnPayment() {
            when(paymentValidator.validatePaymentForRefund(1L, 1L, "USER")).thenReturn(testPayment);
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.refundPayment(1L, refundRequest, 1L, "USER");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when user tries to refund another user's payment")
        void shouldThrowException_whenUserRefundsOtherPayment() {
            when(paymentValidator.validatePaymentForRefund(1L, 2L, "USER"))
                    .thenThrow(new UnauthorizedAccessException("Cannot refund this payment"));

            assertThatThrownBy(() -> paymentService.refundPayment(1L, refundRequest, 2L, "USER"))
                    .isInstanceOf(UnauthorizedAccessException.class)
                    .hasMessageContaining("Cannot refund this payment");
        }

        @Test
        @DisplayName("Should throw exception when payment is not refundable")
        void shouldThrowException_whenPaymentNotRefundable() {
            when(paymentValidator.validatePaymentForRefund(1L, 1L, "ADMIN"))
                    .thenThrow(new BusinessRuleException("Payment cannot be refunded. Current status: PENDING"));

            assertThatThrownBy(() -> paymentService.refundPayment(1L, refundRequest, 1L, "ADMIN"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Payment cannot be refunded");
        }

        @Test
        @DisplayName("Should throw exception when order is shipped or delivered")
        void shouldThrowException_whenOrderShippedOrDelivered() {
            when(paymentValidator.validatePaymentForRefund(1L, 1L, "ADMIN"))
                    .thenThrow(new BusinessRuleException("Cannot refund payment for shipped or delivered orders"));

            assertThatThrownBy(() -> paymentService.refundPayment(1L, refundRequest, 1L, "ADMIN"))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot refund payment for shipped or delivered orders");
        }
    }

    // Partial refund tests

    @Nested
    @DisplayName("Partial Refund Tests")
    class PartialRefundTests {

        @Test
        @DisplayName("Should partially refund payment")
        void shouldPartiallyRefundPayment() {
            testPayment.setStatus(PaymentStatus.COMPLETED);
            refundRequest.setAmount(new BigDecimal("50.00"));
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.partialRefundPayment(1L, refundRequest);

            assertThat(result).isNotNull();
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowException_whenPaymentNotFound() {
            when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.partialRefundPayment(999L, refundRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when payment is not refundable")
        void shouldThrowException_whenPaymentNotRefundableForPartial() {
            testPayment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

            assertThatThrownBy(() -> paymentService.partialRefundPayment(1L, refundRequest))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        @DisplayName("Should throw exception when refund amount is not positive")
        void shouldThrowException_whenRefundAmountNotPositive() {
            refundRequest.setAmount(BigDecimal.ZERO);
            testPayment.setStatus(PaymentStatus.COMPLETED);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

            assertThatThrownBy(() -> paymentService.partialRefundPayment(1L, refundRequest))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    // Fail payment tests

    @Nested
    @DisplayName("Fail Payment Tests")
    class FailPaymentTests {

        @Test
        @DisplayName("Should fail payment when user is admin")
        void shouldFailPayment_whenAdmin() {
            testPayment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.failPayment(1L, "Payment failed", 1L, true);

            assertThat(result).isNotNull();
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Should allow user to fail their own payment")
        void shouldAllowUserToFailTheirOwnPayment() {
            testPayment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.failPayment(1L, "Payment failed", 1L, false);

            assertThat(result).isNotNull();
            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Should throw exception when user tries to fail another user's payment")
        void shouldThrowException_whenUserFailsOtherPayment() {
            testPayment.setStatus(PaymentStatus.PENDING);
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

            assertThatThrownBy(() -> paymentService.failPayment(1L, "Payment failed", 2L, false))
                    .isInstanceOf(UnauthorizedAccessException.class);
        }
    }

    // Admin tests

    @Nested
    @DisplayName("Admin Tests")
    class AdminTests {

        @Test
        @DisplayName("Should return payment for admin")
        void shouldReturnPaymentForAdmin() {
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
            when(paymentMapper.toResponse(any(Payment.class))).thenReturn(testResponse);

            PaymentResponse result = paymentService.findByIdForAdmin(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return all payments for admin")
        void shouldReturnAllPaymentsForAdmin() {
            when(paymentRepository.findAll()).thenReturn(List.of(testPayment));
            when(paymentMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<PaymentResponse> results = paymentService.findAllForAdmin();

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should return refund history")
        void shouldReturnRefundHistory() {
            Payment realPaymentWithRefunds = new Payment.Builder(testOrder, PaymentMethod.CREDIT_CARD, new BigDecimal("150.00"))
                    .build();
            realPaymentWithRefunds.setId(1L);
            realPaymentWithRefunds.setStatus(PaymentStatus.REFUNDED);

            RefundTransaction refundTransaction = new RefundTransaction(
                    realPaymentWithRefunds,
                    new BigDecimal("150.00"),
                    "Test refund",
                    RefundType.FULL
            );
            realPaymentWithRefunds.setRefundTransactions(List.of(refundTransaction));

            when(paymentRepository.findById(1L)).thenReturn(Optional.of(realPaymentWithRefunds));

            List<RefundTransaction> history = paymentService.getRefundHistory(1L);

            assertThat(history).isNotNull();
            assertThat(history).hasSize(1);
        }
    }

    // Scheduled task tests

    @Nested
    @DisplayName("Scheduled Task Tests")
    class ScheduledTaskTests {

        @Test
        @DisplayName("Should cancel expired pending payments")
        void shouldCancelExpiredPendingPayments() {
            Payment expiredPayment = new Payment.Builder(testOrder, PaymentMethod.CREDIT_CARD, new BigDecimal("100.00"))
                    .build();
            expiredPayment.setStatus(PaymentStatus.PENDING);
            expiredPayment.setId(2L);

            when(paymentRepository.findPendingPaymentsOlderThan(any(), any()))
                    .thenReturn(List.of(expiredPayment));

            paymentService.cancelExpiredPendingPayments();

            assertThat(expiredPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            verify(paymentRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should do nothing when no expired payments")
        void shouldDoNothing_whenNoExpiredPayments() {
            when(paymentRepository.findPendingPaymentsOlderThan(any(), any()))
                    .thenReturn(List.of());

            paymentService.cancelExpiredPendingPayments();

            verify(paymentRepository, never()).saveAll(anyList());
        }
    }
}