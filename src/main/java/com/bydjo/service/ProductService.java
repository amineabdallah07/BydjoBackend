package com.bydjo.service;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.product.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    PagedResponse<ProductDto> getAllProducts(int page, int size, String sort, String direction);
    PagedResponse<ProductDto> searchProducts(String search, Long categoryId, boolean isNew, boolean onSale, int page, int size, String sort);
    ProductDto getProductById(Long id);
    ProductDto getProductBySlug(String slug);
    List<ProductDto> getFeaturedProducts();
    List<ProductDto> getNewArrivals();
    List<ProductDto> getBestSellers();
    List<ProductDto> getFlashSaleProducts();
    ProductDto createProduct(ProductCreateDto dto);
    ProductDto updateProduct(Long id, ProductCreateDto dto);
    void deleteProduct(Long id);
    ProductDto uploadProductImages(Long productId, List<MultipartFile> files, Long primaryIndex);
    ProductDto addImageUrls(Long productId, List<ImageUrlDto> images);
    void deleteProductImage(Long productId, Long imageId);
}

