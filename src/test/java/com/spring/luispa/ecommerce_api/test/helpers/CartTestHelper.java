package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartItem;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import com.spring.luispa.ecommerce_api.domain.user.User;

import java.math.BigDecimal;
import java.util.Set;

public class CartTestHelper {

    public static Cart emptyCart(User user) {
        return new Cart(user);
    }

    public static Cart emptyCart(Long id, User user) {
        Cart cart = emptyCart(user);
        cart.setId(id);
        return cart;
    }

    public static Cart activeCart(User user) {
        Cart cart = new Cart(user);
        cart.setId(1L);
        return cart;
    }

    public static Cart createCartWithItems(User user, int productCount) {
        Cart cart = new Cart(user);
        cart.setId(1L);

        for (int i = 1; i <= productCount; i++) {
            Product product = ProductTestHelper.defaultProduct();
            product.setId((long) i);
            product.setSku("LAP-TEST-00" + i);
            product.setPrice(new BigDecimal("1599.99"));
            cart.addItem(product, 2);
        }

        return cart;
    }

    public static Cart createCartWithProduct(User user, Product product, int quantity) {
        Cart cart = new Cart(user);
        cart.setId(1L);
        cart.addItem(product, quantity);
        return cart;
    }

    public static Cart createCartWithMultipleProducts(User user, Set<CartItem> items) {
        Cart cart = new Cart(user);
        cart.setId(1L);

        for (CartItem item : items) {
            cart.addItem(item.getProduct(), item.getQuantity());
        }

        return cart;
    }

    public static Cart inactiveCart(User user) {
        Cart cart = new Cart(user);
        cart.setId(1L);
        cart.setActive(false);
        return cart;
    }

    public static Cart convertedCart(User user) {
        Cart cart = new Cart(user);
        cart.setId(1L);
        cart.markAsConverted();
        return cart;
    }

    public static Cart expiredCart(User user) {
        Cart cart = new Cart(user);
        cart.setId(1L);
        cart.setExpiresAt(java.time.LocalDateTime.now().minusDays(1));
        return cart;
    }
}
