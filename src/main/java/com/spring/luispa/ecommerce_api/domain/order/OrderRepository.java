package com.spring.luispa.ecommerce_api.domain.order;

import com.spring.luispa.ecommerce_api.shared.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface    OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find an order by its number
     * @param orderNumber The order number
     * @return Optional with the Order, if it exists
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find a user's orders based on their ID
     * @param userId The user ID
     * @return List of Order belonging to the user
     */
    List<Order> findByUserId(Long userId);

    /**
     * Returns a page of a user's orders by their ID
     * @param userId The user ID
     * @param pageable
     * @return Page of Order belonging to a user with the specified ID
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Returns a user's orders based on the user ID and order status
     * @param userId The user ID
     * @param status The status of the order
     * @return A user's list of orders filtered by status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Returns a list of orders based on their status
     * @param status The status of the order
     * @return A list of Orders filtered by its status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Returns an order by its ID, including its items
     * @param id The order ID
     * @return Optional with the Order, if it exists
     */
    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);

    /**
     * Returns an order by its ID, including its payment
     * @param id
     * @return
     */
    @EntityGraph(attributePaths = "payment")
    Optional<Order> findWithPaymentById(Long id);

    /**
     * Find order with items, user, and payment
     * @param id The order ID
     * @return Optional with the Order, if it exists
     */
    @Query("""
    SELECT o FROM Order o
    LEFT JOIN FETCH o.items i
    LEFT JOIN FETCH i.product
    LEFT JOIN FETCH o.user
    LEFT JOIN FETCH o.payment
    WHERE o.id = :id""")
    Optional<Order> findOrderDetailById(@Param("id") Long id);

    /**
     * Find recent orders for a user with items
     * @param userId The user ID
     * @param pageable
     * @return List of user's recent orders
     */
    @Query("""
    SELECT o FROM Order o
    LEFT JOIN FETCH o.items
    WHERE o.user.id = :userId
    ORDER BY o.createdAt DESC
    """)
    List<Order> findRecentOrderWithItems(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find the orders within a date range
     * @param startDate The start date
     * @param endDate The end data
     * @param status The order status
     * @return
     */
    @Query("""
    SELECT o FROM Order o
    WHERE o.createdAt BETWEEN :startDate AND :endDate
    AND (:status IS NULL OR o.status = :status)
    """)
    List<Order> findOrderByDateRange(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("status") OrderStatus status);
}
