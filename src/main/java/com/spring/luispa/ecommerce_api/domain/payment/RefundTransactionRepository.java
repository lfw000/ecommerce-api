package com.spring.luispa.ecommerce_api.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    List<RefundTransaction> findByPaymentId(Long paymentId);

    List<RefundTransaction> findByPaymentOrderId(Long orderId);
}
