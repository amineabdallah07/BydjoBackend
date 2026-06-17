package com.bydjo.service.impl;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.product.*;
import com.bydjo.entity.*;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.*;
import com.bydjo.service.FileStorageService;
import com.bydjo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> getAllProducts(int page, int size, String sort, String direction) {
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<Product> products = productRepository.findAllActive(pageable);
        return mapToPagedResponse(products);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> searchProducts(String search, Long categoryId, boolean isNew, boolean onSale, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Page<Product> products;

        // Filtres spéciaux : isNew et onSale (pas de recherche texte combinée)
        if (isNew) {
            products = categoryId != null
                ? productRepository.findActiveNewByCategory(categoryId, pageable)
                : productRepository.findAllActiveNew(pageable);
        } else if (onSale) {
            products = categoryId != null
                ? productRepository.findActiveOnSaleByCategory(categoryId, pageable)
                : productRepository.findAllActiveOnSale(pageable);
        } else if (categoryId != null) {
            if (search != null && !search.isBlank()) {
                products = productRepository.searchActiveProductsByCategory(search, categoryId, pageable);
            } else {
                products = productRepository.findByCategoryId(categoryId, pageable);
            }
        } else if (search != null && !search.isBlank()) {
            products = productRepository.searchActiveProducts(search, pageable);
        } else {
            products = productRepository.findAllActive(pageable);
        }

        return mapToPagedResponse(products);
    }

    @Override
    @Transactional
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    @Override
    @Transactional
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return mapToDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findFeaturedProducts().stream().limit(10)
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getNewArrivals() {
        return productRepository.findNewArrivals(PageRequest.of(0, 10))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getBestSellers() {
        return productRepository.findBestSellers(PageRequest.of(0, 10))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getFlashSaleProducts() {
        return productRepository.findFlashSaleProducts().stream().limit(10)
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductCreateDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .slug(generateSlug(dto.getName()))
                .description(dto.getDescription())
                .details(dto.getDetails())
                .price(dto.getPrice())
                .compareAtPrice(dto.getCompareAtPrice())
                .costPrice(dto.getCostPrice())
                .category(category)
                .brand(dto.getBrand())
                .sku(dto.getSku())
                .featured(dto.getFeatured())
                .isNew(dto.getIsNew())
                .bestseller(dto.getBestseller())
                .flashSale(dto.getFlashSale())
                .isQrProduct(dto.getIsQrProduct() != null && dto.getIsQrProduct())
                .discountPercentage(dto.getDiscountPercentage())
                .flashSaleEndsAt(dto.getFlashSaleEndsAt())
                .material(dto.getMaterial())
                .tags(dto.getTags())
                .active(true)
                .build();

        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductVariantDto vDto : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .stock(vDto.getStock() != null ? vDto.getStock() : 0)
                        .sku(vDto.getSku())
                        .active(true)
                        .build();

                if (vDto.getSizeId() != null) {
                    variant.setSize(sizeRepository.findById(vDto.getSizeId()).orElse(null));
                }
                if (vDto.getColorId() != null) {
                    variant.setColor(colorRepository.findById(vDto.getColorId()).orElse(null));
                }

                product.getVariants().add(variant);
            }
        } else if (dto.getStock() != null && dto.getStock() > 0) {
            // Pas de variantes → créer une variante par défaut avec le stock saisi
            ProductVariant defaultVariant = ProductVariant.builder()
                    .product(product)
                    .stock(dto.getStock())
                    .sku(dto.getSku())
                    .active(true)
                    .build();
            product.getVariants().add(defaultVariant);
        }

        // FIX Bug #5: Calculate and set totalStock before saving
        int totalStock = product.getVariants().stream().mapToInt(v -> v.getStock() != null ? v.getStock() : 0).sum();
        product.setTotalStock(totalStock);

        product = productRepository.save(product);
        return mapToDto(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductCreateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        // FIX Bug #9: Only regenerate slug when the name actually changes (preserves existing URLs/SEO)
        if (!product.getName().equals(dto.getName())) {
            product.setSlug(generateSlug(dto.getName()));
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setDetails(dto.getDetails());
        product.setPrice(dto.getPrice());
        product.setCompareAtPrice(dto.getCompareAtPrice());
        product.setCostPrice(dto.getCostPrice());
        product.setCategory(category);
        product.setBrand(dto.getBrand());
        product.setSku(dto.getSku());
        product.setFeatured(dto.getFeatured());
        product.setIsNew(dto.getIsNew());
        product.setBestseller(dto.getBestseller());
        product.setFlashSale(dto.getFlashSale());
        product.setIsQrProduct(dto.getIsQrProduct() != null && dto.getIsQrProduct());
        product.setDiscountPercentage(dto.getDiscountPercentage());
        product.setFlashSaleEndsAt(dto.getFlashSaleEndsAt());
        product.setMaterial(dto.getMaterial());
        product.setTags(dto.getTags());

        // FIX: Mettre à jour les variantes et recalculer totalStock
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            product.getVariants().clear();
            for (ProductVariantDto vDto : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .stock(vDto.getStock() != null ? vDto.getStock() : 0)
                        .sku(vDto.getSku())
                        .active(true)
                        .build();
                if (vDto.getSizeId() != null) {
                    variant.setSize(sizeRepository.findById(vDto.getSizeId()).orElse(null));
                }
                if (vDto.getColorId() != null) {
                    variant.setColor(colorRepository.findById(vDto.getColorId()).orElse(null));
                }
                product.getVariants().add(variant);
            }
        } else if (dto.getStock() != null) {
            if (product.getVariants().isEmpty()) {
                ProductVariant defaultVariant = ProductVariant.builder()
                        .product(product)
                        .stock(dto.getStock())
                        .sku(dto.getSku())
                        .active(true)
                        .build();
                product.getVariants().add(defaultVariant);
            } else {
                product.getVariants().get(0).setStock(dto.getStock());
            }
        }

        // FIX: Toujours recalculer totalStock depuis les variantes réelles
        int totalStock = product.getVariants().stream()
                .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                .sum();
        product.setTotalStock(totalStock);

        product = productRepository.save(product);
        return mapToDto(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public ProductDto uploadProductImages(Long productId, List<MultipartFile> files, Long primaryIndex) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int sortOrder = product.getImages().size();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String imageUrl = fileStorageService.storeFile(file, "products");

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(imageUrl)
                    .isPrimary(primaryIndex != null && primaryIndex == i)
                    .sortOrder(sortOrder + i)
                    .altText(product.getName())
                    .build();

            product.getImages().add(image);
        }

        product = productRepository.save(product);
        return mapToDto(product);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                + "-" + System.currentTimeMillis() % 10000;
    }

    private ProductDto mapToDto(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .details(p.getDetails())
                .price(p.getPrice())
                .compareAtPrice(p.getCompareAtPrice())
                .costPrice(p.getCostPrice())
                .category(mapCategoryToDto(p.getCategory()))
                .brand(p.getBrand())
                .sku(p.getSku())
                .active(p.getActive())
                .featured(p.getFeatured())
                .isNew(p.getIsNew())
                .bestseller(p.getBestseller())
                .flashSale(p.getFlashSale())
                .isQrProduct(p.getIsQrProduct())
                .discountPercentage(p.getDiscountPercentage())
                .flashSaleEndsAt(p.getFlashSaleEndsAt())
                .material(p.getMaterial())
                .tags(p.getTags())
                .images(p.getImages().stream().map(this::mapImageToDto).collect(Collectors.toList()))
                .variants(p.getVariants().stream().map(this::mapVariantToDto).collect(Collectors.toList()))
                .totalStock(p.getTotalStock())
                .totalSold(p.getTotalSold())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private CategoryDto mapCategoryToDto(Category c) {
        if (c == null) return null;
        return CategoryDto.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .image(c.getImage())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .active(c.getActive())
                .sortOrder(c.getSortOrder())
                .build();
    }

    private ProductImageDto mapImageToDto(ProductImage img) {
        return ProductImageDto.builder()
                .id(img.getId())
                .url(img.getUrl())
                .thumbnailUrl(img.getThumbnailUrl())
                .isPrimary(img.getIsPrimary())
                .sortOrder(img.getSortOrder())
                .altText(img.getAltText())
                .build();
    }

    private ProductVariantDto mapVariantToDto(ProductVariant v) {
        return ProductVariantDto.builder()
                .id(v.getId())
                .sizeId(v.getSize() != null ? v.getSize().getId() : null)
                .sizeName(v.getSize() != null ? v.getSize().getName() : null)
                .colorId(v.getColor() != null ? v.getColor().getId() : null)
                .colorName(v.getColor() != null ? v.getColor().getName() : null)
                .colorHex(v.getColor() != null ? v.getColor().getHexCode() : null)
                .stock(v.getStock())
                .sku(v.getSku())
                .active(v.getActive())
                .build();
    }

    private PagedResponse<ProductDto> mapToPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductDto>builder()
                .content(page.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    @Override
public ProductDto addImageUrls(Long productId, List<ImageUrlDto> images) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit introuvable: " + productId));
 
    // Si l'une des nouvelles images est primary, retirer le flag des existantes
    boolean hasNewPrimary = images.stream().anyMatch(ImageUrlDto::isPrimary);
    if (hasNewPrimary) {
        product.getImages().forEach(img -> img.setIsPrimary(false));
    }
 
    images.forEach(dto -> {
        ProductImage image = ProductImage.builder()
                .url(dto.getUrl())
                .isPrimary(dto.isPrimary())
                .sortOrder(product.getImages().size())
                .product(product)
                .build();
        product.getImages().add(image);
    });
 
    return mapToDto(productRepository.save(product));
}
 
@Override
public void deleteProductImage(Long productId, Long imageId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit introuvable: " + productId));
 
    product.getImages().removeIf(img -> img.getId().equals(imageId));
 
    // Si on vient de supprimer la primary et qu'il reste des images, en désigner une nouvelle
    boolean hasPrimary = product.getImages().stream()
            .anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));
    if (!hasPrimary && !product.getImages().isEmpty()) {
        product.getImages().get(0).setIsPrimary(true);
    }
 
    productRepository.save(product);
}
}