package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.OrderService;
import com.ecommerce.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Payment status pages. Actual charging happens inside CheckoutService as
 * part of the checkout transaction; these endpoints just display the result.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/success/{orderId}")
    public String success(@AuthenticationPrincipal CustomUserDetails principal,
                           @PathVariable Long orderId, Model model) {
        Order order = orderService.getByIdForUser(orderId, userService.getById(principal.getId()));
        model.addAttribute("order", order);
        return "checkout/payment-success";
    }

    @GetMapping("/failed/{orderId}")
    public String failed(@AuthenticationPrincipal CustomUserDetails principal,
                          @PathVariable Long orderId, Model model) {
        Order order = orderService.getByIdForUser(orderId, userService.getById(principal.getId()));
        model.addAttribute("order", order);
        return "checkout/payment-failed";
    }
}
