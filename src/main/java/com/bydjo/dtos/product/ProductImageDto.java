package com.bydjo.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String url;
    private String thumbnailUrl;
    private Boolean isPrimary;
    private Integer sortOrder;
    private String altText;
}
