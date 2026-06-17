package com.bydjo.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String details;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal costPrice;
    private CategoryDto category;
    private String brand;
    private String sku;
    private Boolean active;
    private Boolean featured;
    private Boolean isNew;
    private Boolean bestseller;
    private Boolean flashSale;
    private Boolean isQrProduct;
    private Integer discountPercentage;
    private LocalDateTime flashSaleEndsAt;
    private String material;
    private String tags;
    private List<ProductImageDto> images;
    private List<ProductVariantDto> variants;
    private Integer totalStock;
    private Integer totalSold;
    private Double averageRating;
    private Integer reviewCount;
    private Integer discountAmount;
    private Boolean inWishlist;
    private LocalDateTime createdAt;
}
