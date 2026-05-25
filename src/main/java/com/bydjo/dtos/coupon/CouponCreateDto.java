package com.bydjo.dtos.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateDto {
    private String code;
    private String description;
    private Integer discountPercentage;
    private Integer usageLimit;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private Boolean active;
}
