package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.coupon.CouponValidateDto;
import com.bydjo.entity.Coupon;
import com.bydjo.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponRepository couponRepository;

    // FIX Bug #15: Return minimal DTO — never expose usageLimit, usedCount, dates, etc.
    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<CouponValidateDto>> validateCoupon(@PathVariable String code) {
        return couponRepository.findByCode(code)
                .map(coupon -> {
                    if (coupon.isValid()) {
                        return ResponseEntity.ok(ApiResponse.success("Valid coupon", toDto(coupon)));
                    }
                    return ResponseEntity.ok(ApiResponse.<CouponValidateDto>error("Coupon is no longer valid"));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("Invalid coupon code")));
    }

    private CouponValidateDto toDto(Coupon c) {
        return CouponValidateDto.builder()
                .code(c.getCode())
                .description(c.getDescription())
                .discountPercentage(c.getDiscountPercentage())
                .discountAmount(c.getDiscountAmount())
                .minOrderAmount(c.getMinOrderAmount())
                .maxDiscountAmount(c.getMaxDiscountAmount())
                .build();
    }
}
