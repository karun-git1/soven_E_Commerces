package com.ecommerce.platform.service;

import com.ecommerce.platform.dto.CheckoutRequest;
import com.ecommerce.platform.entity.*;
import com.ecommerce.platform.exception.InsufficientStockException;
import com.ecommerce.platform.exception.OrderException;
import com.ecommerce.platform.exception.PaymentException;
import com.ecommerce.platform.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    /**
     * Places an order from the user's current cart:
     * validates cart & stock, calculates the total, charges payment,
     * then (only on payment success) creates the order, reduces stock
     * and clears the cart - in that order, inside a single transaction.
     */
    @Transactional
    public Order checkout(User user, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(user.getId());

        if (cart.getItems().isEmpty()) {
            throw new OrderException("Your cart is empty");
        }

        // Re-validate stock right before placing the order.
        for (CartItem item : cart.getItems()) {
            Product product = productService.getById(item.getProduct().getId());
            if (!product.isInStock() || product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for " + product.getName() + ". Please update your cart.");
            }
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (Exception e) {
            throw new OrderException("Please select a valid payment method");
        }

        BigDecimal total = cartService.getTotal(cart);

        ShippingAddress address = new ShippingAddress();
        address.setFullName(request.getAddress().getFullName());
        address.setPhone(request.getAddress().getPhone());
        address.setAddressLine(request.getAddress().getAddressLine());
        address.setCity(request.getAddress().getCity());
        address.setState(request.getAddress().getState());
        address.setPostalCode(request.getAddress().getPostalCode());
        address.setCountry(request.getAddress().getCountry());

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setPaymentMethod(method);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        Payment payment = paymentService.processPayment(savedOrder, method);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Payment failed: please try again or use a different method");
        }

        savedOrder.setStatus(OrderStatus.CONFIRMED);

        for (OrderItem orderItem : savedOrder.getItems()) {
            productService.reduceStock(orderItem.getProduct().getId(), orderItem.getQuantity());
        }

        cartService.clearCart(cart);

        return orderRepository.save(savedOrder);
    }
}
