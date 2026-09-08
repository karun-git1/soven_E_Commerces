package com.ecommerce.platform.service;

import com.ecommerce.platform.entity.Cart;
import com.ecommerce.platform.entity.CartItem;
import com.ecommerce.platform.entity.Product;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.exception.InsufficientStockException;
import com.ecommerce.platform.exception.ResourceNotFoundException;
import com.ecommerce.platform.repository.CartItemRepository;
import com.ecommerce.platform.repository.CartRepository;
import com.ecommerce.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = null;
        this.productService = productService;
    }

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserRepository userRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional
    public Cart createCartForUser(User user) {
        Cart cart = new Cart(user);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return cartRepository.save(new Cart(user));
        });
    }

    @Transactional
    public Cart addItem(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.getById(productId);

        if (!product.isInStock() || product.getStock() < quantity) {
            throw new InsufficientStockException("Not enough stock for " + product.getName());
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null) {
            item = new CartItem(cart, product, quantity);
            cart.getItems().add(item);
        } else {
            int newQty = item.getQuantity() + quantity;
            if (newQty > product.getStock()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName());
            }
            item.setQuantity(newQty);
        }
        cartItemRepository.save(item);
        return cart;
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItemInCart(cart, itemId);

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            return cart;
        }

        if (quantity > item.getProduct().getStock()) {
            throw new InsufficientStockException("Not enough stock for " + item.getProduct().getName());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return cart;
    }

    @Transactional
    public Cart removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItemInCart(cart, itemId);
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return cart;
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public BigDecimal getTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItem findItemInCart(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }
}
