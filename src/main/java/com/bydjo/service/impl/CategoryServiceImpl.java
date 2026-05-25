package com.bydjo.service.impl;

import com.bydjo.dtos.product.CategoryDto;
import com.bydjo.entity.Category;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.CategoryRepository;
import com.bydjo.repository.ProductRepository;
import com.bydjo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllActiveWithChildren().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .slug(dto.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"))
                .description(dto.getDescription())
                .image(dto.getImage())
                .active(true)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(dto.getName());
        category.setSlug(dto.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        category.setDescription(dto.getDescription());
        category.setImage(dto.getImage());
        category.setActive(dto.getActive());
        category.setSortOrder(dto.getSortOrder());

        category = categoryRepository.save(category);
        return mapToDto(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setActive(false);
        categoryRepository.save(category);
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
