package com.bydjo.repository;

import com.bydjo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // ✅ FIX MultipleBagFetchException :
    // Hibernate interdit de faire JOIN FETCH sur deux List<> en même temps.
    // On avait : Cart.items (List) + Product.images (List) → CRASH.
    // Solution : on retire "p.images" du JOIN FETCH.
    // Les images se chargent automatiquement car spring.jpa.open-in-view=true
    // garde la session Hibernate ouverte pendant toute la requête HTTP.

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH v.size " +
           "LEFT JOIN FETCH v.color " +
           "WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH v.size " +
           "LEFT JOIN FETCH v.color " +
           "WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);

    void deleteBySessionId(String sessionId);
    void deleteByUserId(Long userId);
}