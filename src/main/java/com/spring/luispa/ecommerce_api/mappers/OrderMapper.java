package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.response.OrderItemResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.OrderResponse;
import com.spring.luispa.ecommerce_api.domain.order.Order;
import com.spring.luispa.ecommerce_api.domain.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class, PaymentMapper.class})
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    @Mapping(source = "billingAddress", target = "billingAddress")
    @Mapping(source = "payment", target = "payment")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.name", target = "productName")
    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);
}
