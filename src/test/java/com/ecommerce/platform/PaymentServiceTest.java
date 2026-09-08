package com.ecommerce.platform;

import com.ecommerce.platform.entity.Order;
import com.ecommerce.platform.entity.Payment;
import com.ecommerce.platform.entity.PaymentMethod;
import com.ecommerce.platform.entity.PaymentStatus;
import com.ecommerce.platform.exception.PaymentException;
import com.ecommerce.platform.payment.MockPaymentGateway;
import com.ecommerce.platform.payment.PaymentResult;
import com.ecommerce.platform.repository.PaymentRepository;
import com.ecommerce.platform.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private MockPaymentGateway paymentGateway;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(paymentRepository, paymentGateway);
    }

    @Test
    void processPayment_marksSuccessOnGatewaySuccess() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("500"));

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.charge(any(), any(), any())).thenReturn(PaymentResult.success("TXN123"));

        Payment payment = paymentService.processPayment(order, PaymentMethod.UPI);

        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("TXN123", payment.getTransactionId());
    }

    @Test
    void processPayment_blocksDoubleChargeOnAlreadySuccessfulOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setTotalAmount(new BigDecimal("500"));

        Payment existing = new Payment();
        existing.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existing));

        assertThrows(PaymentException.class,
                () -> paymentService.processPayment(order, PaymentMethod.UPI));
    }
}
