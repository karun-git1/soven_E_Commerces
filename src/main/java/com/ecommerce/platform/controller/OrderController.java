package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.OrderService;
import com.ecommerce.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping
    public String myOrders(@AuthenticationPrincipal CustomUserDetails principal,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<Order> orders = orderService.getOrdersForUser(principal.getId(), PageRequest.of(page, 10));
        model.addAttribute("orders", orders);
        return "orders/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(@AuthenticationPrincipal CustomUserDetails principal,
                                @PathVariable Long id, Model model) {
        Order order = orderService.getByIdForUser(id, userService.getById(principal.getId()));
        model.addAttribute("order", order);
        return "orders/order-details";
    }

    @GetMapping("/{id}/track")
    public String trackOrder(@AuthenticationPrincipal CustomUserDetails principal,
                              @PathVariable Long id, Model model) {
        Order order = orderService.getByIdForUser(id, userService.getById(principal.getId()));
        model.addAttribute("order", order);
        return "orders/order-tracking";
    }
}
