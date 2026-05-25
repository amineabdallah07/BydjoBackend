package com.bydjo.service;

import com.bydjo.dtos.product.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
    List<CategoryDto> getActiveCategories();
    CategoryDto getCategoryById(Long id);
    CategoryDto createCategory(CategoryDto dto);
    CategoryDto updateCategory(Long id, CategoryDto dto);
    void deleteCategory(Long id);
}
