package com.spring.luispa.ecommerce_api.mappers;

import com.spring.luispa.ecommerce_api.api.dto.response.PaymentResponse;
import com.spring.luispa.ecommerce_api.domain.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
