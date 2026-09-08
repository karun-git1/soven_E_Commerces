package com.ecommerce.platform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {
    private Long productId;
    private Integer quantity = 1;
}
