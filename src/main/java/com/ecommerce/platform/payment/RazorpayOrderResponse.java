package com.ecommerce.platform.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RazorpayOrderResponse {
    private String id;
    private Long amount;
    private String currency;
    private String status;
}