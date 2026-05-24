package com.bydjo.service.impl;

import com.bydjo.dtos.product.ProductDto;
import com.bydjo.entity.Wishlist;
import com.bydjo.repository.WishlistRepository;
import com.bydjo.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductServiceImpl productService;

    @Override
    public List<ProductDto> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(w -> productService.getProductById(w.getProduct().getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToWishlist(Long userId, Long productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            Wishlist wishlist = Wishlist.builder()
                    .user(com.bydjo.entity.User.builder().id(userId).build())
                    .product(com.bydjo.entity.Product.builder().id(productId).build())
                    .build();
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}
