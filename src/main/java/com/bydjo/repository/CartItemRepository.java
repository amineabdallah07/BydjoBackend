package com.bydjo.repository;

import com.bydjo.entity.Cart;
import com.bydjo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByIdAndCart(Long id, Cart cart);
}
