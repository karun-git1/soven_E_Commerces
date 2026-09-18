package com.ecommerce.platform.service;

import com.ecommerce.platform.dto.CheckoutRequest;
import com.ecommerce.platform.entity.Cart;
import com.ecommerce.platform.entity.CartItem;
import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.OrderItem;
import com.ecommerce.platform.entity.OrderStatus;
import com.ecommerce.platform.entity.Payment;
import com.ecommerce.platform.entity.PaymentMethod;
import com.ecommerce.platform.entity.PaymentStatus;
import com.ecommerce.platform.entity.ShippingAddress;
import com.ecommerce.platform.entity.User;
import com.ecommerce.platform.exception.InsufficientStockException;
import com.ecommerce.platform.exception.OrderException;
import com.ecommerce.platform.exception.PaymentException;
import com.ecommerce.platform.payment.RazorpayOrderResponse;
import com.ecommerce.platform.payment.RazorpayPaymentGateway;
import com.ecommerce.platform.repository.OrderRepository;
import com.ecommerce.platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RazorpayCheckoutService {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayPaymentGateway razorpayGateway;

    @Transactional
    public RazorpayCheckoutResult createOrder(User user, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(user.getId());
        if (cart.getItems().isEmpty()) throw new OrderException("Your cart is empty");

        validateStock(cart);
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (Exception e) {
            throw new OrderException("Please select a valid payment method");
        }

        Order order = buildOrder(user, request, method, cartService.getTotal(cart), cart);
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setMethod(method);
        payment.setAmount(savedOrder.getTotalAmount());
        payment.setStatus(PaymentStatus.INITIATED);
        RazorpayOrderResponse gatewayOrder = razorpayGateway.createOrder(
                savedOrder.getTotalAmount(), "SHOP-ORDER-" + savedOrder.getId());
        payment.setRazorpayOrderId(gatewayOrder.getId());
        paymentRepository.save(payment);

        return new RazorpayCheckoutResult(savedOrder.getId(), gatewayOrder.getId(),
                gatewayOrder.getAmount(), gatewayOrder.getCurrency());
    }

    @Transactional
    public Order verifyAndComplete(User user, Long orderId, String gatewayOrderId,
                                   String gatewayPaymentId, String signature) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new PaymentException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) throw new PaymentException("Order not found");
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment session not found"));
        if (payment.getStatus() == PaymentStatus.SUCCESS) return order;
        if (!gatewayOrderId.equals(payment.getRazorpayOrderId())
                || !razorpayGateway.verifySignature(gatewayOrderId, gatewayPaymentId, signature)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(gatewayPaymentId);
        payment.setTransactionId(gatewayPaymentId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
        order.setStatus(OrderStatus.CONFIRMED);
        for (OrderItem item : order.getItems()) {
            productService.reduceStock(item.getProduct().getId(), item.getQuantity());
        }
        cartService.clearCart(cartService.getOrCreateCart(user.getId()));
        return orderRepository.save(order);
    }

    private void validateStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            var product = productService.getById(item.getProduct().getId());
            if (!product.isInStock() || product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName());
            }
        }
    }

    private Order buildOrder(User user, CheckoutRequest request, PaymentMethod method,
                             BigDecimal total, Cart cart) {
        var source = request.getAddress();
        ShippingAddress address = new ShippingAddress();
        address.setFullName(source.getFullName());
        address.setPhone(source.getPhone());
        address.setAddressLine(source.getAddressLine());
        address.setCity(source.getCity());
        address.setState(source.getState());
        address.setPostalCode(source.getPostalCode());
        address.setCountry(source.getCountry());

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setPaymentMethod(method);
        order.setShippingAddress(address);
        for (CartItem cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cartItem.getProduct());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setPriceAtPurchase(cartItem.getProduct().getPrice());
            order.getItems().add(item);
        }
        return order;
    }

    public record RazorpayCheckoutResult(Long orderId, String razorpayOrderId,
                                         Long amount, String currency) { }
}