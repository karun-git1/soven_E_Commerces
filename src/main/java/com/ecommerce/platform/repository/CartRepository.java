package com.ecommerce.platform.repository;

import com.ecommerce.platform.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category"})
    Optional<Cart> findByUserId(Long userId);
}
