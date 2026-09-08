package com.ecommerce.platform;

import com.ecommerce.platform.entity.*;
import com.ecommerce.platform.exception.InsufficientStockException;
import com.ecommerce.platform.repository.CartItemRepository;
import com.ecommerce.platform.repository.CartRepository;
import com.ecommerce.platform.service.CartService;
import com.ecommerce.platform.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductService productService;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cartService = new CartService(cartRepository, cartItemRepository, productService);
    }

    @Test
    void addItem_throwsWhenInsufficientStock() {
        User user = new User();
        user.setId(1L);
        Cart cart = new Cart(user);
        cart.setId(10L);

        Product product = new Product();
        product.setId(5L);
        product.setName("Limited Item");
        product.setStock(1);
        product.setAvailable(true);
        product.setPrice(new BigDecimal("100"));

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productService.getById(5L)).thenReturn(product);

        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(1L, 5L, 5));
    }

    @Test
    void getTotal_sumsSubtotals() {
        Cart cart = new Cart();
        Product p1 = new Product();
        p1.setPrice(new BigDecimal("50"));
        Product p2 = new Product();
        p2.setPrice(new BigDecimal("30"));

        cart.getItems().add(new CartItem(cart, p1, 2));
        cart.getItems().add(new CartItem(cart, p2, 1));

        BigDecimal total = cartService.getTotal(cart);

        assertEquals(new BigDecimal("130"), total);
    }
}
