package com.ecommerce.platform.controller;

import com.ecommerce.platform.config.RazorpayConfig;
import com.ecommerce.platform.dto.CheckoutRequest;
import com.ecommerce.platform.dto.ShippingAddressRequest;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.OrderService;
import com.ecommerce.platform.service.RazorpayCheckoutService;
import com.ecommerce.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Hosts Razorpay's order/verification endpoints and payment status pages.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final OrderService orderService;
    private final UserService userService;
    private final RazorpayCheckoutService razorpayCheckoutService;
    private final RazorpayConfig razorpayConfig;

    @PostMapping("/razorpay/order")
    public ResponseEntity<?> createRazorpayOrder(
            @AuthenticationPrincipal CustomUserDetails principal,
            @ModelAttribute ShippingAddressRequest addressRequest,
            @RequestParam String paymentMethod) {
        if (!razorpayConfig.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Razorpay is not enabled"));
        }
        CheckoutRequest request = new CheckoutRequest();
        request.setAddress(addressRequest);
        request.setPaymentMethod(paymentMethod);
        var result = razorpayCheckoutService.createOrder(userService.getById(principal.getId()), request);
        return ResponseEntity.ok(Map.of(
                "orderId", result.orderId(),
                "razorpayOrderId", result.razorpayOrderId(),
                "amount", result.amount(),
                "currency", result.currency(),
                "keyId", razorpayConfig.getKeyId()));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<?> verifyRazorpayPayment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long orderId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature) {
        Order order = razorpayCheckoutService.verifyAndComplete(
                userService.getById(principal.getId()), orderId, razorpayOrderId,
                razorpayPaymentId, razorpaySignature);
        return ResponseEntity.ok(Map.of("redirectUrl", "/checkout/success/" + order.getId()));
    }

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
