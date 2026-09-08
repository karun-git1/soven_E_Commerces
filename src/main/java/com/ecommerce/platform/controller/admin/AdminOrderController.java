package com.ecommerce.platform.controller.admin;

import com.ecommerce.platform.dto.OrderStatusRequest;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.OrderStatus;
import com.ecommerce.platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Order> orders = orderService.getAllOrders(PageRequest.of(page, 10));
        model.addAttribute("orders", orders);
        return "admin/orders/orders";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Order order = orderService.getDetailedById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/order-details";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @ModelAttribute OrderStatusRequest request) {
        orderService.updateStatus(id, request);
        return "redirect:/admin/orders/" + id + "?updated=true";
    }
}
