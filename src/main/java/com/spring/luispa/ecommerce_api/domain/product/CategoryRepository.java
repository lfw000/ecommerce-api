package com.spring.luispa.ecommerce_api.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByName(String name);

    List<Category> findByActiveTrue();

    List<Category> findByParentCategoryIsNull();

    List<Category> findByParentCategoryId(Long parentId);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveOrdered();

    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC")
    List<Category> findAllOrdered();
}
