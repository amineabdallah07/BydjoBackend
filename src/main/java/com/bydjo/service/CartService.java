package com.bydjo.service;

import com.bydjo.dtos.cart.AddToCartDto;
import com.bydjo.dtos.cart.CartDto;

public interface CartService {
    CartDto getCart(Long userId, String sessionId);
    CartDto addToCart(Long userId, String sessionId, AddToCartDto dto);
    CartDto updateCartItem(Long userId, String sessionId, Long itemId, Integer quantity);
    CartDto removeFromCart(Long userId, String sessionId, Long itemId);
    void clearCart(Long userId, String sessionId);
    CartDto applyCoupon(Long userId, String sessionId, String couponCode);
    CartDto removeCoupon(Long userId, String sessionId);
}
