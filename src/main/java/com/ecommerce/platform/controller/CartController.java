package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.Cart;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Cart cart = cartService.getOrCreateCart(principal.getId());
        model.addAttribute("cart", cart);
        model.addAttribute("total", cartService.getTotal(cart));
        return "cart/cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long productId,
                             @RequestParam(defaultValue = "1") int quantity,
                             @RequestParam(required = false) String redirectTo) {
        cartService.addItem(principal.getId(), productId, quantity);
        if (redirectTo != null && redirectTo.equals("cart")) {
            return "redirect:/cart";
        }
        return "redirect:/products/" + productId + "?added=true";
    }

    @PostMapping("/update/{itemId}")
    public String updateQuantity(@AuthenticationPrincipal CustomUserDetails principal,
                                  @PathVariable Long itemId,
                                  @RequestParam int quantity) {
        cartService.updateQuantity(principal.getId(), itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@AuthenticationPrincipal CustomUserDetails principal,
                              @PathVariable Long itemId) {
        cartService.removeItem(principal.getId(), itemId);
        return "redirect:/cart";
    }
}
