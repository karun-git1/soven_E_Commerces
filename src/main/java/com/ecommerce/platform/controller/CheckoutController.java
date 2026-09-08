package com.ecommerce.platform.controller;

import com.ecommerce.platform.dto.CheckoutRequest;
import com.ecommerce.platform.dto.ShippingAddressRequest;
import com.ecommerce.platform.entity.Cart;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.PaymentMethod;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.CartService;
import com.ecommerce.platform.service.CheckoutService;
import com.ecommerce.platform.service.OrderService;
import com.ecommerce.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Cart cart = cartService.getOrCreateCart(principal.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartService.getTotal(cart));
        model.addAttribute("addressRequest", new ShippingAddressRequest());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "checkout/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@AuthenticationPrincipal CustomUserDetails principal,
                              @ModelAttribute ShippingAddressRequest addressRequest,
                              @RequestParam("paymentMethod") String paymentMethod,
                              Model model) {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddress(addressRequest);
        request.setPaymentMethod(paymentMethod);

        Order order = checkoutService.checkout(userService.getById(principal.getId()), request);

        return "redirect:/checkout/success/" + order.getId();
    }

    @GetMapping("/success/{orderId}")
    public String orderSuccess(@AuthenticationPrincipal CustomUserDetails principal,
                                @PathVariable Long orderId,
                                Model model) {
        Order order = orderService.getByIdForUser(orderId, userService.getById(principal.getId()));
        model.addAttribute("order", order);
        return "checkout/order-success";
    }
}
