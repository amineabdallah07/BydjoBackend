package com.bydjo.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    private Long id;
    private Long sizeId;
    private String sizeName;
    private Long colorId;
    private String colorName;
    private String colorHex;
    private Integer stock;
    private String sku;
    private Boolean active;
}
