package com.bydjo.service;

import com.bydjo.dtos.product.ProductDto;

import java.util.List;

public interface WishlistService {
    List<ProductDto> getUserWishlist(Long userId);
    void addToWishlist(Long userId, Long productId);
    void removeFromWishlist(Long userId, Long productId);
    boolean isInWishlist(Long userId, Long productId);
}
