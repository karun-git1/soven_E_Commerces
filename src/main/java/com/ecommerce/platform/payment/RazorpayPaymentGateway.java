package com.ecommerce.platform.payment;

import com.ecommerce.platform.entity.PaymentMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Placeholder integration for a real gateway (e.g. Razorpay/Stripe).
 * Wire this up with the provider's SDK and enable it via
 * app.payment.provider=razorpay in application.properties.
 * Currently disabled/not selected by default - see PaymentServiceConfig.
 */
@Component
public class RazorpayPaymentGateway implements PaymentGateway {

    @Value("${app.payment.razorpay.key:}")
    private String apiKey;

    @Value("${app.payment.razorpay.secret:}")
    private String apiSecret;

    @Override
    public PaymentResult charge(BigDecimal amount, PaymentMethod method, String orderReference) {
        // TODO: integrate real Razorpay/Stripe SDK call here using apiKey/apiSecret.
        throw new UnsupportedOperationException(
                "Real payment gateway is not configured yet. Use the mock gateway for now.");
    }
}
