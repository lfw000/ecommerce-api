package com.spring.luispa.ecommerce_api.test.helpers;

import com.spring.luispa.ecommerce_api.api.dto.response.OrderResponse;
import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResponseTestHelper {

    public static OrderResponse defaultOrderResponse() {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setOrderNumber("ORD-TEST-001");
        response.setUserId(1L);
        response.setUserEmail("test@example.com");
        response.setStatus(OrderStatus.PENDING);
        response.setSubtotal(new BigDecimal("3199.98"));
        response.setShippingCost(new BigDecimal("10.00"));
        response.setTaxAmount(new BigDecimal("320.00"));
        response.setTotalAmount(new BigDecimal("3529.98"));
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    public static OrderResponse defaultOrderResponse(Long id) {
        OrderResponse response = defaultOrderResponse();
        response.setId(id);
        return response;
    }

    public static OrderResponse orderResponseWithStatus(OrderStatus status) {
        OrderResponse response = defaultOrderResponse();
        response.setStatus(status);
        return response;
    }

    public static OrderResponse orderResponseWithUser(Long userId, String email) {
        OrderResponse response = defaultOrderResponse();
        response.setUserId(userId);
        response.setUserEmail(email);
        return response;
    }

}
