package com.spring.luispa.ecommerce_api.unit.services;

import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.CreateOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.OrderResponse;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.AddressRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.OrderMapper;
import com.spring.luispa.ecommerce_api.services.OrderService;
import com.spring.luispa.ecommerce_api.services.calculation.OrderCalculator;
import com.spring.luispa.ecommerce_api.services.factory.OrderFactory;
import com.spring.luispa.ecommerce_api.services.management.StockManager;
import com.spring.luispa.ecommerce_api.services.validation.OrderValidator;
import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.shared.exception.*;
import com.spring.luispa.ecommerce_api.test.helpers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    // Mocks

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private OrderCalculator orderCalculator;

    @Mock
    private StockManager stockManager;

    @Mock
    private OrderFactory orderFactory;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private LoggingAspect loggingAspect;

    private OrderService orderService;

    // Test data

    private User testUser;
    private Address testAddress;
    private Cart testCart;
    private Order testOrder;
    private OrderResponse testResponse;
    private CreateOrderRequest createRequest;
    private CancelOrderRequest cancelRequest;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                cartRepository,
                paymentRepository,
                orderMapper,
                loggingAspect,
                orderValidator,
                orderCalculator,
                stockManager,
                orderFactory
        );

        testUser = UserTestHelper.defaultUser(1L);
        testAddress = AddressTestHelper.defaultAddress(1L, testUser);
        testCart = CartTestHelper.createCartWithItems(testUser, 2);
        testOrder = OrderTestHelper.minimalOrder(1L);
        testResponse = ResponseTestHelper.defaultOrderResponse();

        createRequest = new CreateOrderRequest();
        createRequest.setShippingAddressId(1L);
        createRequest.setBillingAddressId(1L);
        createRequest.setShippingMethod("standard");

        cancelRequest = new CancelOrderRequest(
                CancellationReason.USER_REQUESTED,
                "Changed my mind");
    }

    // 1. Creation tests

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order when cart has items and addresses are valid")
        void shouldCreateOrder_whenCartHasItems() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L)).thenReturn(testCart);
            when(orderValidator.validateAddress(1L, 1L)).thenReturn(testAddress);
            when(orderCalculator.calculateSubtotal(testCart)).thenReturn(new BigDecimal("3199.98"));
            when(orderCalculator.calculateShippingCost(testCart)).thenReturn(BigDecimal.ZERO);
            when(orderCalculator.calculateTax(any(BigDecimal.class))).thenReturn(new BigDecimal("319.998"));

            Set<OrderItem> orderItems = Set.of(mock(OrderItem.class));
            when(orderFactory.createOrder(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(testOrder);

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.createOrderFromCart(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_whenUserNotFound() {
            when(orderValidator.validateUser(1L))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw exception when cart is empty")
        void shouldThrowException_whenCartIsEmpty() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L))
                    .thenThrow(new BusinessRuleException("Cannot create order from empty cart"));

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Cannot create order from empty cart");
        }

        @Test
        @DisplayName("Should throw exception when product has insufficient stock")
        void shouldThrowException_whenStockInsufficient() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L)).thenReturn(testCart);
            doThrow(new BusinessRuleException("Insufficient stock"))
                    .when(orderValidator).validateStock(testCart);

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("Should throw exception when address not found")
        void shouldThrowException_whenAddressNotFound() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L)).thenReturn(testCart);
            when(orderValidator.validateAddress(1L, 1L))
                    .thenThrow(new ResourceNotFoundException("Address not found"));

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Address not found");
        }

        @Test
        @DisplayName("Should throw exception when address does not belong to user")
        void shouldThrowException_whenAddressNotOwnedByUser() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L)).thenReturn(testCart);
            when(orderValidator.validateAddress(1L, 1L))
                    .thenThrow(new BusinessRuleException("Address does not belong to the user"));

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Address does not belong to the user");
        }

        @Test
        @DisplayName("Should mark cart as converted after order creation")
        void shouldMarkCartAsConverted() {
            when(orderValidator.validateUser(1L)).thenReturn(testUser);
            when(orderValidator.validateCart(1L)).thenReturn(testCart);
            when(orderValidator.validateAddress(1L, 1L)).thenReturn(testAddress);
            when(orderCalculator.calculateSubtotal(testCart)).thenReturn(new BigDecimal("3199.98"));
            when(orderCalculator.calculateShippingCost(testCart)).thenReturn(BigDecimal.ZERO);
            when(orderCalculator.calculateTax(any(BigDecimal.class))).thenReturn(new BigDecimal("319.998"));

            Set<OrderItem> orderItems = Set.of(mock(OrderItem.class));
            when(orderFactory.createOrder(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(testOrder);

            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            orderService.createOrderFromCart(1L, createRequest);

            verify(stockManager).reserveStock(testCart);
            verify(cartRepository).save(testCart);
        }
    }

    // 2. Basic query tests

    @Nested
    @DisplayName("Basic Query Tests")
    class BasicQueryTests {

        @Test
        @DisplayName("Should return order by ID")
        void shouldReturnOrder_whenIdExists() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when order not found by ID")
        void shouldThrowException_whenIdNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");
        }

        @Test
        @DisplayName("Should return order by order number")
        void shouldReturnOrder_whenOrderNumberExists() {
            when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findByOrderNumber("ORD-001");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository).findByOrderNumber("ORD-001");
        }

        @Test
        @DisplayName("Should throw exception when order number not found")
        void shouldThrowException_whenOrderNumberNotFound() {
            when(orderRepository.findByOrderNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findByOrderNumber("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found with order number");
        }

        @Test
        @DisplayName("Should return order detail by ID")
        void shouldReturnOrderDetail_whenIdExists() {
            when(orderRepository.findOrderDetailById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findOrderDetailById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(orderRepository).findOrderDetailById(1L);
        }

        @Test
        @DisplayName("Should throw exception when order detail not found")
        void shouldThrowException_whenOrderDetailNotFound() {
            when(orderRepository.findOrderDetailById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findOrderDetailById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // 3. Query tests for users

    @Nested
    @DisplayName("User Query Tests")
    class UserQueryTests {

        @Test
        @DisplayName("Should return order when user is owner")
        void shouldReturnOrder_whenUserIsOwner() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findByIdForUser(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when order belongs to another user")
        void shouldThrowException_whenOrderBelongsToAnotherUser() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.findByIdForUser(1L, 2L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Order does not belong to user");
        }

        @Test
        @DisplayName("Should return order by order number for user")
        void shouldReturnOrderByOrderNumber_whenUserIsOwner() {
            when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findByOrderNumberForUser("ORD-001", 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when order number belongs to another user")
        void shouldThrowException_whenOrderNumberBelongsToAnotherUser() {
            when(orderRepository.findByOrderNumber("ORD-001")).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.findByOrderNumberForUser("ORD-001", 2L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Order does not belong to user");
        }

        @Test
        @DisplayName("Should return order detail when user is owner")
        void shouldReturnOrderDetail_whenUserIsOwner() {
            when(orderRepository.findOrderDetailById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findOrderDetailByIdForUser(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when order detail belongs to another user")
        void shouldThrowException_whenOrderDetailBelongsToAnotherUser() {
            when(orderRepository.findOrderDetailById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.findOrderDetailByIdForUser(1L, 2L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Order does not belong to user");
        }

        @Test
        @DisplayName("Should return orders for user")
        void shouldReturnOrdersForUser() {
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<OrderResponse> results = orderService.findByUserId(1L);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowException_whenUserNotFound() {
            when(orderValidator.validateUser(1L))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should return orders by status for user")
        void shouldReturnOrdersByStatus_forUser() {
            when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<OrderResponse> results = orderService.findByUserIdAndStatus(1L, OrderStatus.PENDING);

            assertThat(results).hasSize(1);
            verify(orderRepository).findByUserIdAndStatus(1L, OrderStatus.PENDING);
        }

        @Test
        @DisplayName("Should return recent orders with items")
        void shouldReturnRecentOrdersWithItems() {
            when(orderRepository.findRecentOrderWithItems(eq(1L), any(Pageable.class))).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<OrderResponse> results = orderService.findRecentOrdersWithItems(1L, 5);

            assertThat(results).hasSize(1);
            verify(orderRepository).findRecentOrderWithItems(eq(1L), any(Pageable.class));
        }
    }

    // 4. Status update tests

    @Nested
    @DisplayName("Order Status Update Tests")
    class OrderStatusTests {

        @Test
        @DisplayName("Should confirm payment when order is pending")
        void shouldConfirmPayment_whenOrderIsPending() {
            Order pendingOrder = OrderTestHelper.orderWithStatus(OrderStatus.PENDING);
            pendingOrder.setId(1L);
            Payment payment = mock(Payment.class);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.confirmPayment(1L, "tx-123");

            assertThat(result).isNotNull();
            assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(payment).complete(eq("tx-123"), isNull());
            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should throw exception when payment not found")
        void shouldThrowException_whenPaymentNotFound() {
            Order pendingOrder = OrderTestHelper.orderWithStatus(OrderStatus.PENDING);
            pendingOrder.setId(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.confirmPayment(1L, "tx-123"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Payment not found");
        }

        @Test
        @DisplayName("Should throw exception when confirming payment for non-pending order")
        void shouldThrowException_whenOrderNotPending() {
            Order paidOrder = OrderTestHelper.orderWithStatus(OrderStatus.PAID);
            paidOrder.setId(1L);
            Payment mockPayment = mock(Payment.class);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(mockPayment));

            assertThatThrownBy(() -> orderService.confirmPayment(1L, "tx-123"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot confirm payment. Current status: PAID");
        }

        @Test
        @DisplayName("Should ship order when order is paid")
        void shouldShipOrder_whenOrderIsPaid() {
            Order paidOrder = OrderTestHelper.processingOrder();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.shipOrder(1L, "TRACK-123");

            assertThat(result).isNotNull();
            assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(paidOrder.getTrackingNumber()).isEqualTo("TRACK-123");
        }

        @Test
        @DisplayName("Should throw exception when shipping non-paid order")
        void shouldThrowException_whenShippingNonPaidOrder() {
            Order pendingOrder = OrderTestHelper.orderWithStatus(OrderStatus.PENDING);
            pendingOrder.setId(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderService.shipOrder(1L, "TRACK-123"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should deliver order when order is shipped")
        void shouldDeliverOrder_whenOrderIsShipped() {
            Order shippedOrder = OrderTestHelper.shippedOrder();
            shippedOrder.setId(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.deliverOrder(1L);

            assertThat(result).isNotNull();
            assertThat(shippedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("Should throw exception when delivering non-shipped order")
        void shouldThrowException_whenDeliveringNonShippedOrder() {
            Order paidOrder = OrderTestHelper.paidOrder(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(paidOrder));

            assertThatThrownBy(() -> orderService.deliverOrder(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // 5. Cancellation tests

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order when user is owner and order is pending")
        void shouldCancelOrder_whenUserIsOwnerAndOrderIsPending() {
            Order pendingOrder = OrderTestHelper.orderWithStatus(OrderStatus.PENDING);
            pendingOrder.setId(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.cancelOrder(1L, cancelRequest, 1L, "USER");

            assertThat(result).isNotNull();
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowException_whenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(999L, cancelRequest, 1L, "USER"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when user is not owner and not admin")
        void shouldThrowException_whenUserIsNotOwner() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, cancelRequest, 2L, "USER"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Order does not belong to user");
        }

        @Test
        @DisplayName("Should allow admin to cancel any order")
        void shouldAllowAdminToCancelAnyOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.cancelOrder(1L, cancelRequest, 2L, "ADMIN");

            assertThat(result).isNotNull();
            verify(orderRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when cancelling already cancelled order")
        void shouldThrowException_whenOrderAlreadyCancelled() {
            Order cancelledOrder = OrderTestHelper.cancelledOrder(1L);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, cancelRequest, 1L, "USER"))
                    .isInstanceOf(OrderCancellationNotAllowedException.class)
                    .hasMessageContaining("Order cannot be cancelled in current status: CANCELLED");
        }

        @Test
        @DisplayName("Should throw exception when cancelling paid order after 30 minutes")
        void shouldThrowException_whenCancellingPaidOrderAfter30Minutes() {
            Order oldPaidOrder = OrderTestHelper.paidOrder(1L);
            oldPaidOrder.setCreatedAt(LocalDateTime.now().minusMinutes(31));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(oldPaidOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, cancelRequest, 1L, "USER"))
                    .isInstanceOf(OrderCancellationWindowExpiredException.class)
                    .hasMessageContaining("Cancellation window has expired (30 minutes after order)");
        }

        @Test
        @DisplayName("Should allow admin to cancel without time restriction")
        void shouldAllowAdminToCancelWithoutTimeRestriction() {
            Order oldPaidOrder = OrderTestHelper.paidOrder(1L);
            oldPaidOrder.setCreatedAt(LocalDateTime.now().minusMinutes(31));

            when(orderRepository.findById(1L)).thenReturn(Optional.of(oldPaidOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.cancelOrder(1L, cancelRequest, 1L, "ADMIN");

            assertThat(result).isNotNull();
            assertThat(oldPaidOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    // 6. Admin tests

    @Nested
    @DisplayName("Admin Order Tests")
    class AdminOrderTests {

        @Test
        @DisplayName("Should return order for admin")
        void shouldReturnOrderForAdmin() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.findByIdForAdmin(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return all orders for admin")
        void shouldReturnAllOrdersForAdmin() {
            when(orderRepository.findAll()).thenReturn(List.of(testOrder));
            when(orderMapper.toResponseList(anyList())).thenReturn(List.of(testResponse));

            List<OrderResponse> results = orderService.findAllForAdmin();

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should confirm payment for admin")
        void shouldConfirmPaymentForAdmin() {
            Order pendingOrder = OrderTestHelper.orderWithStatus(OrderStatus.PENDING);
            pendingOrder.setId(1L);
            Payment payment = mock(Payment.class);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
            when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.confirmPaymentForAdmin(1L, "tx-123");

            assertThat(result).isNotNull();
            assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(payment).complete(eq("tx-123"), isNull());
            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should ship order for admin")
        void shouldShipOrderForAdmin() {
            Order processingOrder = OrderTestHelper.processingOrder();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(processingOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.shipOrderForAdmin(1L, "TRACK-123");

            assertThat(result).isNotNull();
            assertThat(processingOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(processingOrder.getTrackingNumber()).isEqualTo("TRACK-123");
        }

        @Test
        @DisplayName("Should deliver order for admin")
        void shouldDeliverOrderForAdmin() {
            Order shippedOrder = OrderTestHelper.shippedOrder();
            shippedOrder.setId(1L);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(testResponse);

            OrderResponse result = orderService.deliverOrderForAdmin(1L);

            assertThat(result).isNotNull();
            assertThat(shippedOrder.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }
}