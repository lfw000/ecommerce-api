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
import com.spring.luispa.ecommerce_api.domain.user.AddressRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.mappers.OrderMapper;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import com.spring.luispa.ecommerce_api.shared.exception.ResourceNotFoundException;
import com.spring.luispa.ecommerce_api.shared.exception.UnauthorizedException;
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

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        CartRepository cartRepository,
                        AddressRepository addressRepository,
                        PaymentRepository paymentRepository,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.orderMapper = orderMapper;
    }

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

    public List<OrderResponse> findByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findCartForCheckout(userId)
                .orElseThrow(() -> new BusinessRuleException("No active cart found for user"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cannot create order from empty cart");
        }

        validateStock(cart);

        Address shippingAddress = getAddress(request.getShippingAddressId(), userId);
        Address billingAddress = getAddress(request.getBillingAddressId(), userId);

        Set<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> new OrderItem.Builder(
                        cartItem.getProduct(),
                        cartItem.getQuantity())
                        .unitPrice(cartItem.getPrice())
                        .build())
                .collect(Collectors.toSet());

        BigDecimal subtotal = cart.getTotalAmount();
        BigDecimal shippingCost = calculateShippingCost(cart);
        BigDecimal taxAmount = calculateTax(subtotal);

        Order order = new Order.Builder(user, shippingAddress, billingAddress, orderItems)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .shippingMethod(request.getShippingMethod())
                .estimateDeliveryDate(LocalDateTime.now().plusDays(5))
                .notes(request.getNotes())
                .build();

        Order savedOrder = orderRepository.save(order);

        updateStock(cart);

        cart.markAsConverted();

        cartRepository.save(cart);

        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse confirmPayment(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.confirmPayment(transactionId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        payment.complete(transactionId, null);

        paymentRepository.save(payment);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.ship(trackingNumber);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.deliver();

        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request, Long userId, String userRole ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!"ADMIN".equals(userRole) && !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Order does not belong to user");
        }

        order.cancel(request, userId, userRole);

        return orderMapper.toResponse(order);
    }

    // Helper methods

    private void validateStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasStock(item.getQuantity())) {
                throw new BusinessRuleException(String.format("Insufficient stock for product: %s. Available. %d, Requested: %d",
                        product.getSku(),
                        product.getStock(),
                        item.getQuantity()));
            }

        }
    }

    private void updateStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
        }
    }

    private Address getAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("Address does not belong to the user");
        }

        return address;
    }

    private BigDecimal calculateShippingCost(Cart cart) {
        BigDecimal total = cart.getTotalAmount();
        if (total.compareTo(new BigDecimal("100")) >= 0) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal("10.00");
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.10"));
    }

    //private void createPendingPayment(Order order) {
    //    Payment payment = new Payment.Builder(order, PaymentMethod.CARD, order.getTotalAmount())
    //            .build();
    //
    //    paymentRepository.save(payment);
    //}

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

}
