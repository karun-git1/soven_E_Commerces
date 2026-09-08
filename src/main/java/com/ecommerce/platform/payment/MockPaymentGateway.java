package com.ecommerce.platform.payment;

import com.ecommerce.platform.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment gateway used for development/testing.
 * Always succeeds unless the amount is a "magic" trigger value used in tests,
 * so QA can exercise both the success and failure flows.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final BigDecimal FORCE_FAILURE_AMOUNT = new BigDecimal("13.00");

    @Override
    public PaymentResult charge(BigDecimal amount, PaymentMethod method, String orderReference) {
        if (method == PaymentMethod.COD) {
            // Cash on delivery is collected later, not at checkout time.
            return PaymentResult.success("COD-" + orderReference);
        }

        if (amount.compareTo(FORCE_FAILURE_AMOUNT) == 0) {
            return PaymentResult.failure("Payment declined by bank");
        }

        String transactionId = "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return PaymentResult.success(transactionId);
    }
}
