package com.ecommerce.platform.service;

import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.Payment;
import com.ecommerce.platform.entity.PaymentMethod;
import com.ecommerce.platform.entity.PaymentStatus;
import com.ecommerce.platform.exception.PaymentException;
import com.ecommerce.platform.payment.MockPaymentGateway;
import com.ecommerce.platform.payment.PaymentResult;
import com.ecommerce.platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    // Using the mock gateway directly for now; swap for a PaymentGateway bean
    // selection strategy once a real provider (Razorpay/Stripe) is enabled.
    private final MockPaymentGateway paymentGateway;

    @Transactional
    public Payment processPayment(Order order, PaymentMethod method) {
        // Guard against double-charging an already successfully paid order.
        Payment existing = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentException("This order has already been paid for");
        }

        Payment payment = existing != null ? existing : new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        PaymentResult result = paymentGateway.charge(order.getTotalAmount(), method, "ORDER-" + order.getId());

        if (result.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(result.getTransactionId());
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }
}
