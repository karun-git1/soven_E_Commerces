package com.ecommerce.platform.controller.admin;

import com.ecommerce.platform.dto.CategoryRequest;
import com.ecommerce.platform.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "admin/categories/categories";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("categoryRequest", new CategoryRequest());
        return "admin/categories/add-category";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var category = categoryService.getById(id);
        CategoryRequest request = new CategoryRequest();
        request.setName(category.getName());
        request.setDescription(category.getDescription());
        model.addAttribute("category", category);
        model.addAttribute("categoryRequest", request);
        return "admin/categories/edit-category";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/add-category";
        }
        try {
            categoryService.create(request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/categories/add-category";
        }
        return "redirect:/admin/categories?added=true";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute CategoryRequest request) {
        categoryService.update(id, request);
        return "redirect:/admin/categories?updated=true";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/admin/categories?deleted=true";
    }
}
