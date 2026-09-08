package com.ecommerce.platform.payment;

import com.ecommerce.platform.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {

    /**
     * Charges the given amount using the given payment method.
     * Implementations should never throw for a declined/failed payment -
     * they should return a failed PaymentResult instead.
     */
    PaymentResult charge(BigDecimal amount, PaymentMethod method, String orderReference);
}
