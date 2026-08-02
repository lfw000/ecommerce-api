package com.spring.luispa.ecommerce_api.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByName(String name);

    List<Category> findByActiveTrue();

    List<Category> findByParentCategoryIsNull();

    List<Category> findByParentCategoryId(Long parentId);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.active = true")
    Optional<Category> findActiveById(@Param("id") Long id);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveOrdered();

    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC")
    List<Category> findAllOrdered();

    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL AND c.active = true")
    List<Category> findActiveRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId AND c.active = true")
    List<Category> findActiveByParentCategoryId(@Param("parentId") Long parentId);
}
