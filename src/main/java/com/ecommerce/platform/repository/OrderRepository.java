package com.ecommerce.platform.repository;

import com.ecommerce.platform.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("select o from Order o join fetch o.user order by o.createdAt desc")
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select distinct o from Order o "
            + "left join fetch o.user "
            + "left join fetch o.items i "
            + "left join fetch i.product "
            + "left join fetch o.shippingAddress "
            + "left join fetch o.payment "
            + "where o.id = :id")
    Optional<Order> findDetailedById(Long id);
}
