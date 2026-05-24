package com.bydjo.repository;

import com.bydjo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndApprovedTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);
    long countByProductId(Long productId);
    void deleteByUserId(Long userId);
}
