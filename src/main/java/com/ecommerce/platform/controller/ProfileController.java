package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.security.CustomUserDetails;
import com.ecommerce.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = userService.getById(principal.getId());
        model.addAttribute("user", user);
        return "profile/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address,
                                 Model model) {
        User user = userService.updateProfile(principal.getId(), name, phone, address);
        model.addAttribute("user", user);
        model.addAttribute("updated", true);
        return "profile/profile";
    }
}
