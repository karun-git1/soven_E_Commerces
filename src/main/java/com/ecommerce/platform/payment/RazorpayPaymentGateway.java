package com.ecommerce.platform.payment;

import com.ecommerce.platform.config.RazorpayConfig;
import com.ecommerce.platform.entity.PaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Razorpay REST API integration. Checkout.js owns sensitive payment details;
 * this class creates gateway orders and verifies signatures.
 */
@Component
@RequiredArgsConstructor
public class RazorpayPaymentGateway implements PaymentGateway {

    private final RazorpayConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public PaymentResult charge(BigDecimal amount, PaymentMethod method, String orderReference) {
        throw new UnsupportedOperationException(
                "Razorpay uses the browser Checkout flow. Create a gateway order before charging.");
    }

    public RazorpayOrderResponse createOrder(BigDecimal amount, String receipt) {
        requireConfigured();
        long amountInMinorUnits = amount.movePointRight(2).longValueExact();
        String json = "{\"amount\":" + amountInMinorUnits
                + ",\"currency\":\"" + config.getCurrency()
                + "\",\"receipt\":\"" + escapeJson(receipt) + "\"}";

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/orders"))
                .header("Authorization", basicAuth())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Razorpay order creation failed: HTTP " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), RazorpayOrderResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create Razorpay order", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Could not create Razorpay order", e);
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        requireConfigured();
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(config.getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = hmac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(digest, hexToBytes(signature));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private String basicAuth() {
        String credentials = config.getKeyId() + ":" + config.getKeySecret();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void requireConfigured() {
        if (config.getKeyId().isBlank() || config.getKeySecret().isBlank()) {
            throw new IllegalStateException("Razorpay is enabled but API keys are missing");
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static byte[] hexToBytes(String value) {
        if (value == null || value.length() % 2 != 0) throw new IllegalArgumentException("Invalid signature");
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(value.substring(i, i + 2), 16);
        }
        return result;
    }
}
