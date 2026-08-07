package com.spring.luispa.ecommerce_api.services.management;

import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import com.spring.luispa.ecommerce_api.domain.cart.CartItem;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import com.spring.luispa.ecommerce_api.domain.product.Product;
import org.springframework.stereotype.Component;

@Component
public class StockManager {

    public void reserveStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
        }
    }

    public void releaseStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
        }
    }

    public boolean isStockSufficient(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (!product.hasStock(item.getQuantity())) {
                return false;
            }
        }
        return true;
    }
}
