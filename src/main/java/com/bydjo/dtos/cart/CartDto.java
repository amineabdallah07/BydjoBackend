package com.bydjo.dtos.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private List<CartItemDto> items;
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String couponCode;
}
