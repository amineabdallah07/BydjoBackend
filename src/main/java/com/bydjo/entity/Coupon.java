package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer discountPercentage = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageLimit = 100;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startsAt) && now.isBefore(expiresAt) && usedCount < usageLimit;
    }
}
