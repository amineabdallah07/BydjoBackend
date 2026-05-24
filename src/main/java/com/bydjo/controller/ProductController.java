package com.bydjo.controller;
import com.bydjo.dtos.product.ImageUrlsRequest;
import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.product.*;
import com.bydjo.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sort, direction));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductDto>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "false") boolean isNew,
            @RequestParam(defaultValue = "false") boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        return ResponseEntity.ok(productService.searchProducts(q, category, isNew, onSale, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDto> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductDto>> getNewArrivals() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<List<ProductDto>> getBestSellers() {
        return ResponseEntity.ok(productService.getBestSellers());
    }

    @GetMapping("/flash-sale")
    public ResponseEntity<List<ProductDto>> getFlashSaleProducts() {
        return ResponseEntity.ok(productService.getFlashSaleProducts());
    }

    // Admin endpoints
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductCreateDto dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductCreateDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductDto> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "primaryIndex", required = false) Long primaryIndex) {
        return ResponseEntity.ok(productService.uploadProductImages(id, files, primaryIndex));
    }

    // In ProductController.java — add this method alongside the other image endpoint

@PostMapping("/{id}/images/urls")
public ResponseEntity<ProductDto> addImageUrls(
        @PathVariable Long id,
        @RequestBody ImageUrlsRequest request) {
    return ResponseEntity.ok(productService.addImageUrls(id, request.getImages()));
}
}
