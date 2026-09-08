package com.ecommerce.platform.controller;

import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.service.CategoryService;
import com.ecommerce.platform.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/products")
    public String list(@RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<Product> products = productService.browse(categoryId, search, PageRequest.of(page, 12));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String details(@PathVariable Long id, Model model) {
        Product product = productService.getDetailedById(id);
        model.addAttribute("product", product);
        return "product-details";
    }
}
