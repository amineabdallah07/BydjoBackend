package com.bydjo.dtos.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductCreateDto {
    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    private String description;
    private String details;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @DecimalMin(value = "0")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0")
    private BigDecimal costPrice;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String brand;
    private String sku;

    private Boolean featured = false;
    private Boolean isNew = false;
    private Boolean bestseller = false;
    private Boolean flashSale = false;
    private Integer discountPercentage = 0;
    private LocalDateTime flashSaleEndsAt;
    private String material;
    private String tags;
    private Integer stock;
    private List<ProductVariantDto> variants;
}