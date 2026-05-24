package com.bydjo.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String image;
    private Long parentId;
    private Boolean active;
    private Integer sortOrder;
    private Integer productCount;
    private List<CategoryDto> children;
}
