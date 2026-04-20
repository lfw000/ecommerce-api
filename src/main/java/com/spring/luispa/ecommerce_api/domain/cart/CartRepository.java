package com.spring.luispa.ecommerce_api.domain.cart;

import com.spring.luispa.ecommerce_api.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndActiveTrue(Long userId);

    boolean existsByUserIdAndActiveTrue(Long userId);

    @Query("""
    SELECT c FROM Cart c
    LEFT JOIN FETCH c.items i
    LEFT JOIN FETCH i.product
    WHERE c.user.id = :userId AND c.active = true""")
    Optional<Cart> findActiveCartWithItems(@Param("userId") Long userId);

    @Query("""
    SELECT c FROM Cart c
    LEFT JOIN FETCH c.items i
    LEFT JOIN FETCH i.product p
    LEFT JOIN FETCH p.category
    WHERE c.user.id = :userId AND c.active = true""")
    Optional<Cart> findCartForCheckout(@Param("userId") Long userId);
}
