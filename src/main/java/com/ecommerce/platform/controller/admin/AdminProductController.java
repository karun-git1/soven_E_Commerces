package com.ecommerce.platform.controller.admin;

import com.ecommerce.platform.dto.ProductRequest;
import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.service.CategoryService;
import com.ecommerce.platform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> products = productService.getAllForAdmin(PageRequest.of(page, 10));
        model.addAttribute("products", products);
        return "admin/products/products";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.getAll());
        return "admin/products/add-product";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("productRequest") ProductRequest request,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            return "admin/products/add-product";
        }
        productService.create(request);
        return "redirect:/admin/products?added=true";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        request.setStock(product.getStock());
        request.setAvailable(product.isAvailable());

        model.addAttribute("productRequest", request);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        return "admin/products/edit-product";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                        @Valid @ModelAttribute("productRequest") ProductRequest request,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("product", productService.getById(id));
            return "admin/products/edit-product";
        }
        productService.update(id, request);
        return "redirect:/admin/products?updated=true";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/admin/products?deleted=true";
    }

    @PostMapping("/toggle-availability/{id}")
    public String toggleAvailability(@PathVariable Long id, @RequestParam boolean available) {
        productService.setAvailability(id, available);
        return "redirect:/admin/products";
    }
}
