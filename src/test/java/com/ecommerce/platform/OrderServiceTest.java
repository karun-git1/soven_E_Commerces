package com.ecommerce.platform;

import com.ecommerce.platform.dto.OrderStatusRequest;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.OrderStatus;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.exception.ResourceNotFoundException;
import com.ecommerce.platform.repository.OrderRepository;
import com.ecommerce.platform.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void getByIdForUser_throwsWhenOrderBelongsToAnotherUser() {
        User owner = new User();
        owner.setId(1L);
        Order order = new Order();
        order.setId(100L);
        order.setUser(owner);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        User otherUser = new User();
        otherUser.setId(2L);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getByIdForUser(100L, otherUser));
    }

    @Test
    void updateStatus_changesOrderStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus("SHIPPED");

        Order updated = orderService.updateStatus(1L, request);

        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
    }
}
