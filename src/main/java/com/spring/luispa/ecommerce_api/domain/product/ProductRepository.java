package com.spring.luispa.ecommerce_api.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Product> findBySku(String sku);

    @EntityGraph(attributePaths = {"category", "images"})
    @Query("SELECT p FROM Product p WHERE p.sku = :sku AND p.active = true")
    Optional<Product> findActiveBySku(@Param("sku") String sku);

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByActiveTrue(Pageable pageable);

    boolean existsBySku(String sku);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findByPriceBetweenAndActiveTrue(BigDecimal min, BigDecimal max);

    List<Product> findByPriceLessThanAndActiveTrue(BigDecimal max);

    List<Product> findByPriceGreaterThanAndActiveTrue(BigDecimal min);

    @EntityGraph(attributePaths = {"category", "images"})
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    Optional<Product> findActiveWithCategoryAndImagesById(@Param(("id")) Long id);

    @EntityGraph(attributePaths = {"category", "images"})
    Optional<Product> findWithCategoryAndImagesById(Long id);

    @Query("""
    SELECT p FROM Product p
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.images
    WHERE p.featured = true AND p.active = true
    ORDER BY p.createdAt DESC""")
    List<Product> findFeaturedWithImages(Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.images
    WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))""")
    Page<Product> searchAllProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.images
    WHERE p.active = true
    AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))""")
    Page<Product> searchActiveProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT p FROM Product p
    WHERE p.category.id = :categoryId
    AND p.active = true
    AND p.price BETWEEN :minPrice AND :maxPrice""")
    List<Product> findByCategoryAndPriceRange(
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
    SELECT p FROM Product p
    WHERE p.active = true
    AND p.stock <= p.lowStockThreshold""")
    List<Product> findLowStockProducts();
}
