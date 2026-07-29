package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.api.dto.request.CancelOrderRequest;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.shared.enums.CancellationReason;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.Set;

public class OrderTestHelper {

    // Default items

    public static OrderItem defaultOrderItem() {
        Product product = ProductTestHelper.defaultProduct();
        product.setId(1L);
        return new OrderItem.Builder(product, 2)
                .unitPrice(product.getPrice())
                .build();
    }

    public static Set<OrderItem> defaultOrderItems() {
        return Set.of(defaultOrderItem());
    }

    public static Set<OrderItem> orderItemsWithQuantity(int quantity) {
        Product product = ProductTestHelper.defaultProduct();
        product.setId(1L);
        OrderItem item = new OrderItem.Builder(product, quantity)
                .unitPrice(product.getPrice())
                .build();
        return Set.of(item);
    }

    public static Set<OrderItem> orderItemsWithDiscount(int quantity, int discountPercentage) {
        Product product = ProductTestHelper.defaultProduct();
        product.setId(1L);
        OrderItem item = new OrderItem.Builder(product, quantity)
                .unitPrice(product.getPrice())
                .withDiscount(discountPercentage)
                .build();
        return Set.of(item);
    }

    public static Set<OrderItem> orderItemsWithMultipleProducts() {
        Product product1 = ProductTestHelper.defaultProduct();
        product1.setId(1L);
        product1.setSku("LAP-001");
        product1.setPrice(new BigDecimal("1599.99"));

        Product product2 = ProductTestHelper.defaultProduct();
        product2.setId(2L);
        product2.setSku("MOU-001");
        product2.setPrice(new BigDecimal("45.99"));

        OrderItem item1 = new OrderItem.Builder(product1, 1)
                .unitPrice(product1.getPrice())
                .build();

        OrderItem item2 = new OrderItem.Builder(product2, 2)
                .unitPrice(product2.getPrice())
                .build();

        return Set.of(item1, item2);
    }

    // Orders creation

    public static Order minimalOrder() {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order minimalOrder(Long id) {
        Order order = minimalOrder();
        order.setId(id);
        return order;
    }

    public static Order orderWithStatus(OrderStatus status) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .status(status)
                .build();
    }

    public static Order orderWithStatus(Long id, OrderStatus status) {
        Order order = orderWithStatus(status);
        order.setId(id);
        return order;
    }

    public static Order orderWithUser(User user) {
        Address address = AddressTestHelper.defaultAddress(user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order orderWithUserAndAddress(User user, Address address) {
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order orderWithItems(Set<OrderItem> items) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);

        return new Order.Builder(user, address, address, items)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order orderWithShippingAndBillingAddress(User user, Address shipping, Address billing) {
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, shipping, billing, items)
                .status(OrderStatus.PENDING)
                .build();
    }

    // Orders with specific status

    public static Order cancelledOrder() {
        Order order = minimalOrder();
        order.cancel(
                new CancelOrderRequest(CancellationReason.USER_REQUESTED, "Test cancellation"),
                1L,
                "USER"
        );
        return order;
    }

    public static Order cancelledOrder(Long id) {
        Order order = cancelledOrder();
        order.setId(id);
        return order;
    }

    public static Order paidOrder() {
        Order order = minimalOrder();
        order.confirmPayment("tx-test-123");
        return order;
    }

    public static Order paidOrder(Long id) {
        Order order = paidOrder();
        order.setId(id);
        return order;
    }

    public static Order processingOrder() {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        Order order = new Order.Builder(user, address, address, items)
                .status(OrderStatus.PROCESSING)
                .build();
        order.setId(1L);
        return order;
    }

    public static Order shippedOrder() {
        Order order = paidOrder();
        order.startProcessing();
        order.ship("TRACK-123");
        return order;
    }

    public static Order deliveredOrder() {
        Order order = shippedOrder();
        order.deliver();
        return order;
    }

    // Orders with specific fields

    public static Order orderWithNotes(String notes) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .notes(notes)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order orderWithShippingMethod(String shippingMethod) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .shippingMethod(shippingMethod)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Order orderWithTotals(BigDecimal subtotal, BigDecimal shippingCost, BigDecimal taxAmount) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Set<OrderItem> items = defaultOrderItems();

        return new Order.Builder(user, address, address, items)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .status(OrderStatus.PENDING)
                .build();
    }
}
