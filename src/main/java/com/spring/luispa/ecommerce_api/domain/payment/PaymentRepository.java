package com.spring.luispa.ecommerce_api.domain.payment;

import com.spring.luispa.ecommerce_api.shared.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    @Query("""
    SELECT p FROM Payment p
    LEFT JOIN FETCH p.order o
    WHERE p.id = :id""")
    Optional<Payment> findPaymentWithOrder(@Param("id") Long id);

    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("""
    SELECT p FROM Payment p
    JOIN p.order o
    WHERE o.user.id = :userId
    ORDER BY p.createdAt DESC""")
    List<Payment> findPaymentsByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT p FROM Payment p
    WHERE p.status = :status
    AND p.createdAt < :date""")
    List<Payment> findPendingPaymentsOlderThan(
        @Param("status") PaymentStatus status,
        @Param("date") LocalDateTime date);
}
