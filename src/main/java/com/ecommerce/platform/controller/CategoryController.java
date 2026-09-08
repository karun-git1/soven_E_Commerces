package com.ecommerce.platform.controller;

import com.ecommerce.platform.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "categories";
    }
}
