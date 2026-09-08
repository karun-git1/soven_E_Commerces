package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.service.CategoryService;
import com.ecommerce.platform.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featuredProducts",
                productService.browse(null, null, PageRequest.of(0, 8)).getContent());
        model.addAttribute("categories", categoryService.getAll());
        return "home";
    }
}
