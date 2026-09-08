package com.ecommerce.platform.service;

import com.ecommerce.platform.dto.OrderStatusRequest;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.OrderStatus;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.exception.ResourceNotFoundException;
import com.ecommerce.platform.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<Order> getOrdersForUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public Order getDetailedById(Long id) {
        return orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public Order getByIdForUser(Long id, User user) {
        Order order = getDetailedById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order;
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatusRequest request) {
        Order order = getById(id);
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
