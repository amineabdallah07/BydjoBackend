package com.bydjo.controller;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.entity.Product;
import com.bydjo.entity.Review;
import com.bydjo.entity.User;
import com.bydjo.repository.ProductRepository;
import com.bydjo.repository.ReviewRepository;
import com.bydjo.repository.UserRepository;
import com.bydjo.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review APIs")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<Review>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Review> reviews = reviewRepository.findByProductIdAndApprovedTrueOrderByCreatedAtDesc(
                productId, PageRequest.of(page, size));
        PagedResponse<Review> response = PagedResponse.<Review>builder()
                .content(reviews.getContent())
                .pageNumber(reviews.getNumber())
                .pageSize(reviews.getSize())
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .last(reviews.isLast())
                .first(reviews.isFirst())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> addReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> reviewData) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userPrincipal != null ?
                userRepository.findById(userPrincipal.getId()).orElse(null) : null;

        Review review = Review.builder()
                .product(product)
                .user(user)
                .guestName(user == null ? (String) reviewData.get("guestName") : null)
                .rating((Integer) reviewData.getOrDefault("rating", 5))
                .title((String) reviewData.get("title"))
                .comment((String) reviewData.get("comment"))
                .approved(false)
                .build();
        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
