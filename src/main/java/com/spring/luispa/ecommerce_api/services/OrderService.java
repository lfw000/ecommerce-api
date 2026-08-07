package com.spring.luispa.ecommerce_api.services;


import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.CreateOrderRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.OrderResponse;
import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartItem;
import com.spring.luispa.ecommerce_api.domain.cart.CartRepository;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import com.spring.luispa.ecommerce_api.domain.order.OrderRepository;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import com.spring.luispa.ecommerce_api.domain.payment.PaymentRepository;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
import com.spring.luispa.ecommerce_api.mappers.OrderMapper;
import com.spring.luispa.ecommerce_api.services.calculation.OrderCalculator;
import com.spring.luispa.ecommerce_api.services.factory.OrderFactory;
import com.spring.luispa.ecommerce_api.services.management.StockManager;
import com.spring.luispa.ecommerce_api.services.validation.OrderValidator;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.OrderCancellationNotAllowedException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final OrderValidator orderValidator;
    private final OrderCalculator orderCalculator;
    private final StockManager stockManager;
    private final OrderFactory orderFactory;
    private final OrderMapper orderMapper;
    private final LoggingAspect loggingAspect;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        PaymentRepository paymentRepository,
                        OrderMapper orderMapper, LoggingAspect loggingAspect, OrderValidator orderValidator, OrderCalculator orderCalculator, StockManager stockManager, OrderFactory orderFactory) {
        this.orderRepository = orderRepository;
        this.orderCalculator = orderCalculator;
        this.stockManager = stockManager;
        this.orderFactory = orderFactory;
        this.cartRepository = cartRepository;
        this.paymentRepository = paymentRepository;
        this.orderMapper = orderMapper;
        this.loggingAspect = loggingAspect;
        this.orderValidator = orderValidator;
    }

    // Methods for regular users

    public OrderResponse findByIdForUser(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        return orderMapper.toResponse(order);
    }

    public OrderResponse findByOrderNumberForUser(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        return orderMapper.toResponse(order);
    }

    public OrderResponse findOrderDetailByIdForUser(Long orderId, Long userId) {
        Order order = orderRepository.findOrderDetailById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> findByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return orderMapper.toResponseList(orders);
    }

    public List<OrderResponse> findByUserIdAndStatus(Long userId, OrderStatus status) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);

        return orderMapper.toResponseList(orders);
    }

    public List<OrderResponse> findRecentOrdersWithItems(Long userId, int limit) {
        Pageable limitPage = Pageable.ofSize(limit);

        List<Order> orders = orderRepository.findRecentOrderWithItems(userId, limitPage);

        return orderMapper.toResponseList(orders);
    }

    @Transactional
    public OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);
        loggingAspect.setUserIdInMDC(userId);

        User user = orderValidator.validateUser(userId);
        Cart cart = orderValidator.validateCart(userId);
        orderValidator.validateStock(cart);

        Address shippingAddress = orderValidator.validateAddress(request.getShippingAddressId(), userId);
        Address billingAddress = orderValidator.validateAddress(request.getBillingAddressId(), userId);

        log.debug("Creating order from cart with {} items, subtotal: {}", cart.getItems().size(),
                cart.getTotalAmount());

        Set<OrderItem> orderItems = cart.getItems().stream()
                        .map(cartItem -> new OrderItem.Builder(
                                cartItem.getProduct(),
                                cartItem.getQuantity())
                                .unitPrice(cartItem.getPrice())
                                .build())
                        .collect(Collectors.toSet());

        BigDecimal subtotal = orderCalculator.calculateSubtotal(cart);
        BigDecimal shippingCost = orderCalculator.calculateShippingCost(cart);
        BigDecimal taxAmount = orderCalculator.calculateTax(subtotal);

        Order order = orderFactory.createOrder(
                user,
                shippingAddress,
                billingAddress,
                orderItems,
                subtotal,
                shippingCost,
                taxAmount,
                request.getShippingMethod(),
                request.getNotes());

        Order savedOrder = orderRepository.save(order);

        stockManager.reserveStock(cart);
        cart.markAsConverted();
        cartRepository.save(cart);

        log.info("Order created successfully, orderId={}, userId={}, total={}, items={}",
                savedOrder.getId(), userId, savedOrder.getTotalAmount(), savedOrder.getItems().size());

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse confirmPayment(Long orderId, String transactionId) {
        log.info("Confirming payment for order: orderId={}, transactionId={}", orderId, transactionId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for payment confirmation: order={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment not found for order: order={}", orderId);
                    return new ResourceNotFoundException("Payment not found for order: " + orderId);
                });

        order.confirmPayment(transactionId);

        payment.complete(transactionId, null);

        paymentRepository.save(payment);

        log.info("Payment confirmed: orderId={}, transactionId={}", orderId, transactionId);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        log.info("Shipping order: orderId={}, trackingNumber={}", orderId, trackingNumber);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.ship(trackingNumber);

        log.info("Order shipped: orderId={}, trackingNumber={}", orderId, trackingNumber);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        log.info("Delivering order: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.deliver();

        log.info("Order delivered: orderId={}", orderId);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, Long userId, String userRole ) {
        log.info("Cancelling order: orderId={}, userId={}, userRole={}, reason={}", orderId, userId, userRole,
                request.getReason());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for cancellation: orderId={}", orderId);
                    return new ResourceNotFoundException("Order not found with id: " + orderId);
                });

        if (!"ADMIN".equals(userRole) && !order.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to cancel order {} belonging to user {}",
                    userId, orderId, order.getUser().getId());
            throw new UnauthorizedException("Order does not belong to user");
        }

        if (!order.isCancellable()) {
            log.warn("Cannot cancel order: orderId={}, currentStatus={}", orderId, order.getStatus());
            throw new OrderCancellationNotAllowedException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.cancel(request, userId, userRole);
        
        if (order.getPayment() != null && order.getPayment().isRefundable()) {
            stockManager.releaseStock(order);
            log.debug("Stock released for cancelled order: orderId={}", orderId);
        }

        log.info("Order cancelled: orderId={}, userId={}, reason={}, role={}", orderId, userId, request.getReason(),
                userRole);

        return orderMapper.toResponse(order);
    }

    // Administrator methods

    public OrderResponse findByIdForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> findAllForAdmin() {
        return orderMapper.toResponseList(orderRepository.findAll());
    }

    @Transactional
    public OrderResponse confirmPaymentForAdmin(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId).
                orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.confirmPayment(transactionId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        payment.complete(transactionId, null);

        paymentRepository.save(payment);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrderForAdmin(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.ship(trackingNumber);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrderForAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.deliver();
        return orderMapper.toResponse(order);
    }

    // Common queries

    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return orderMapper.toResponse(order);
    }

    public OrderResponse findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        return orderMapper.toResponse(order);
    }

    public OrderResponse findOrderDetailById(Long id) {
        Order order = orderRepository.findOrderDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        return orderMapper.toResponse(order);
    }

}
