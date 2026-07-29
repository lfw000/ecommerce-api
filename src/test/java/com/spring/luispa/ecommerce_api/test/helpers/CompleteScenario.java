package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;

public class CompleteScenario {

    private final User user;
    private final Address address;
    private final Product product;
    private final Cart cart;
    private final Order order;

    private CompleteScenario(Builder builder) {
        this.user = builder.user;
        this.address = builder.address;
        this.product = builder.product;
        this.cart = builder.cart;
        this.order = builder.order;
    }

    public User getUser() {
        return user;
    }

    public Address getAddress() {
        return address;
    }

    public Product getProduct() {
        return product;
    }

    public Cart getCart() {
        return cart;
    }

    public Order getOrder() {
        return order;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;
        private Address address;
        private Product product;
        private Cart cart;
        private Order order;

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withAddress(Address address) {
            this.address = address;
            return this;
        }

        public Builder withProduct(Product product) {
            this.product = product;
            return this;
        }

        public Builder withCart(Cart cart) {
            this.cart = cart;
            return this;
        }

        public Builder withOrder(Order order) {
            this.order = order;
            return this;
        }

        public CompleteScenario build() {
            // Default values
            if (user == null) {
                user = UserTestHelper.defaultUser(1L);
            }
            if (address == null) {
                address = AddressTestHelper.defaultAddress(1L, user);
            }
            if (product == null) {
                product = ProductTestHelper.defaultProduct(1L);
            }
            if (cart == null) {
                cart = CartTestHelper.createCartWithProduct(user, product, 2);
            }
            if (order == null) {
                order = OrderTestHelper.minimalOrder(1L);
            }

            return new CompleteScenario(this);
        }
    }

    // Static methods for common scenarios

    public static CompleteScenario createDefaultScenario() {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Product product = ProductTestHelper.defaultProduct(1L);
        Cart cart = CartTestHelper.createCartWithProduct(user, product, 2);
        Order order = OrderTestHelper.minimalOrder(1L);

        return CompleteScenario.builder()
                .withUser(user)
                .withAddress(address)
                .withProduct(product)
                .withCart(cart)
                .withOrder(order)
                .build();
    }

    public static CompleteScenario createScenarioWithStock(int stock) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Product product = ProductTestHelper.productWithStock(stock);
        product.setId(1L);
        Cart cart = CartTestHelper.createCartWithProduct(user, product, 2);
        Order order = OrderTestHelper.minimalOrder(1L);

        return CompleteScenario.builder()
                .withUser(user)
                .withAddress(address)
                .withProduct(product)
                .withCart(cart)
                .withOrder(order)
                .build();
    }

    public static CompleteScenario createScenarioWithOrderStatus(OrderStatus status) {
        User user = UserTestHelper.defaultUser(1L);
        Address address = AddressTestHelper.defaultAddress(1L, user);
        Product product = ProductTestHelper.defaultProduct(1L);
        Cart cart = CartTestHelper.createCartWithProduct(user, product, 2);
        Order order = OrderTestHelper.orderWithStatus(status);
        order.setId(1L);

        return CompleteScenario.builder()
                .withUser(user)
                .withAddress(address)
                .withProduct(product)
                .withCart(cart)
                .withOrder(order)
                .build();
    }
}
