package com.ecommerce.platform.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RazorpayConfig {

    @Value("${app.payment.razorpay.key:}")
    private String keyId;

    @Value("${app.payment.razorpay.secret:}")
    private String keySecret;

    @Value("${app.payment.razorpay.currency:INR}")
    private String currency;

    @Value("${app.payment.provider:mock}")
    private String provider;

    public boolean isEnabled() {
        return "razorpay".equalsIgnoreCase(provider);
    }
}