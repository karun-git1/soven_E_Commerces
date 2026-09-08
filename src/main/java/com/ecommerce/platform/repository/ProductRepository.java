package com.ecommerce.platform.repository;

import com.ecommerce.platform.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findByAvailableTrue(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdAndAvailableTrue(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByNameContainingIgnoreCaseAndAvailableTrue(String name, Pageable pageable);

    @Query("select p from Product p left join fetch p.category where p.id = :id")
    Optional<Product> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);
}
