package com.ecommerce.platform;

import com.ecommerce.platform.dto.ProductRequest;
import com.ecommerce.platform.entity.Category;
import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.repository.ProductRepository;
import com.ecommerce.platform.service.CategoryService;
import com.ecommerce.platform.service.FileStorageService;
import com.ecommerce.platform.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryService categoryService;
    @Mock private FileStorageService fileStorageService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository, categoryService, fileStorageService);
    }

    @Test
    void createProduct_setsFieldsFromRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Wireless Mouse");
        request.setDescription("Ergonomic wireless mouse");
        request.setPrice(new BigDecimal("799.00"));
        request.setCategoryId(1L);
        request.setStock(50);
        request.setAvailable(true);

        Category category = new Category("Electronics", "Gadgets");
        when(categoryService.getById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.create(request);

        assertEquals("Wireless Mouse", saved.getName());
        assertEquals(new BigDecimal("799.00"), saved.getPrice());
        assertEquals(50, saved.getStock());
        assertTrue(saved.isAvailable());
        assertEquals(category, saved.getCategory());
    }

    @Test
    void reduceStock_neverGoesNegative() {
        Product product = new Product();
        product.setStock(3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        productService.reduceStock(1L, 5);

        assertEquals(0, product.getStock());
    }
}
