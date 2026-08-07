package com.spring.luispa.ecommerce_api.services.calculation;

import com.spring.luispa.ecommerce_api.domain.cart.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderCalculator {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("10.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public BigDecimal calculateSubtotal(Cart cart) {
        return cart.getTotalAmount();
    }

    public BigDecimal calculateShippingCost(Cart cart) {
        BigDecimal total = cart.getTotalAmount();
        if (total.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return SHIPPING_COST;
    }

    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(TAX_RATE);
    }

    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal shippingCost, BigDecimal tax) {
        return subtotal.add(shippingCost).add(tax);
    }

    public BigDecimal calculateItemSubtotal(BigDecimal unitPrice, Integer quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}