package com.ecommerce.platform.config;

import jakarta.annotation.PostConstruct;
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

    public boolean isConfigured() {
        return isEnabled()
                && keyId != null && !keyId.isBlank()
                && keySecret != null && !keySecret.isBlank();
    }

    @PostConstruct
    public void logConfigurationStatus() {
        String keyPreview = keyId == null || keyId.isBlank()
                ? "MISSING"
                : keyId.substring(0, Math.min(12, keyId.length())) + "...";
        System.out.println("PAYMENT PROVIDER: " + provider);
        System.out.println("RAZORPAY KEY: " + keyPreview);
        System.out.println("RAZORPAY SECRET: " + (keySecret == null || keySecret.isBlank() ? "MISSING" : "LOADED"));
        System.out.println("CURRENCY: " + currency);
    }
}