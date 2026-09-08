package com.ecommerce.platform.service;

import com.ecommerce.platform.dto.ProductRequest;
import com.ecommerce.platform.entity.Category;
import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.exception.ResourceNotFoundException;
import com.ecommerce.platform.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    public Page<Product> browse(Long categoryId, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return productRepository.findByNameContainingIgnoreCaseAndAvailableTrue(search, pageable);
        }
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndAvailableTrue(categoryId, pageable);
        }
        return productRepository.findByAvailableTrue(pageable);
    }

    public Page<Product> getAllForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public Product getDetailedById(Long id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        applyRequest(product, request);
        return productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest request) {
        Category category = categoryService.getById(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setAvailable(request.isAvailable());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileStorageService.store(request.getImage());
            product.setImageUrl(imageUrl);
        }
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Product setAvailability(Long id, boolean available) {
        Product product = getById(id);
        product.setAvailable(available);
        return productRepository.save(product);
    }

    @Transactional
    public void reduceStock(Long productId, int quantity) {
        Product product = getById(productId);
        product.setStock(product.getStock() - quantity);
        if (product.getStock() <= 0) {
            product.setStock(0);
        }
        productRepository.save(product);
    }
}
