package com.bydjo.controller;

import com.bydjo.dtos.cart.AddToCartDto;
import com.bydjo.dtos.cart.CartDto;
import com.bydjo.security.UserPrincipal;
import com.bydjo.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.getCart(userId, sessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addToCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId,
            @Valid @RequestBody AddToCartDto dto) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.addToCart(userId, sessionId, dto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.updateCartItem(userId, sessionId, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeFromCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId,
            @PathVariable Long itemId) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.removeFromCart(userId, sessionId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        cartService.clearCart(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartDto> applyCoupon(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId,
            @RequestParam String code) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.applyCoupon(userId, sessionId, code));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartDto> removeCoupon(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String sessionId) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        return ResponseEntity.ok(cartService.removeCoupon(userId, sessionId));
    }
}
