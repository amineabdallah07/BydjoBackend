package com.bydjo.controller;

import com.bydjo.dtos.product.ProductDto;
import com.bydjo.security.UserPrincipal;
import com.bydjo.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist APIs")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(wishlistService.getUserWishlist(userPrincipal.getId()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> addToWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long productId) {
        wishlistService.addToWishlist(userPrincipal.getId(), productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long productId) {
        wishlistService.removeFromWishlist(userPrincipal.getId(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> isInWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isInWishlist(userPrincipal.getId(), productId));
    }
}
