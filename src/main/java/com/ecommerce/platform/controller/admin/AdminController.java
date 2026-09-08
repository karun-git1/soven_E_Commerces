package com.ecommerce.platform.controller.admin;

import com.ecommerce.platform.repository.OrderRepository;
import com.ecommerce.platform.repository.ProductRepository;
import com.ecommerce.platform.repository.UserRepository;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        long totalProducts = productRepository.count();
        long totalCustomers = userRepository.count();
        List<Order> recentOrders = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)).getContent();
        long totalOrders = orderRepository.count();

        BigDecimal totalSales = recentOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.CONFIRMED)
                .count();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("totalSales", totalSales);
        return "admin/dashboard";
    }
}
