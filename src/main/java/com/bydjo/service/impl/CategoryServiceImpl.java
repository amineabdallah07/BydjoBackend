package com.bydjo.service.impl;

import com.bydjo.dtos.product.CategoryDto;
import com.bydjo.entity.Category;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.CategoryRepository;
import com.bydjo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllActiveWithChildren().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToDto(category);
    }

    private CategoryDto mapToDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .image(c.getImage())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .active(c.getActive())
                .sortOrder(c.getSortOrder())
                .children(c.getChildren() != null ? c.getChildren().stream()
                        .filter(ch -> ch.getActive())
                        .map(this::mapToDto).collect(Collectors.toList()) : null)
                .build();
    }
}
