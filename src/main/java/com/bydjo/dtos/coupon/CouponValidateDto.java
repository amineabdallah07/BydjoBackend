package com.bydjo.dtos.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * FIX Bug #15: Minimal DTO for public coupon validation — never exposes usageLimit, usedCount, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidateDto {
    private String code;
    private String description;
    private Integer discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
}
