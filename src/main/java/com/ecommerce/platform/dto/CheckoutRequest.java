package com.ecommerce.platform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {
    private ShippingAddressRequest address;
    private String paymentMethod;
}
