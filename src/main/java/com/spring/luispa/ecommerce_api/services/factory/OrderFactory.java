package com.spring.luispa.ecommerce_api.services.factory;

import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import com.spring.luispa.ecommerce_api.domain.user.Address;
import com.spring.luispa.ecommerce_api.domain.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class OrderFactory {

    public Order createOrder(User user,
                             Address shippingAddress,
                             Address billingAddress,
                             Set<OrderItem> orderItems,
                             BigDecimal subtotal,
                             BigDecimal shippingCost,
                             BigDecimal taxAmount,
                             String shippingMethod,
                             String notes) {

        return new Order.Builder(user, shippingAddress, billingAddress, orderItems)
                .shippingCost(shippingCost)
                .taxAmount(taxAmount)
                .shippingMethod(shippingMethod)
                .estimateDeliveryDate(LocalDateTime.now().plusDays(5))
                .notes(notes)
                .build();
    }

    public Order createOrder(User user,
                             Address shippingAddress,
                             Address billingAddress,
                             Set<OrderItem> orderItems) {
        return new Order.Builder(user, shippingAddress, billingAddress, orderItems)
                .estimateDeliveryDate(LocalDateTime.now().plusDays(5))
                .build();
    }
}