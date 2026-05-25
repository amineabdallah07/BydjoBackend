package com.bydjo.repository;

import com.bydjo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.featured = true ORDER BY p.createdAt DESC")
    List<Product> findFeaturedProducts();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.isNew = true ORDER BY p.createdAt DESC")
    List<Product> findNewArrivals(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.bestseller = true ORDER BY p.totalSold DESC")
    List<Product> findBestSellers(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.flashSale = true ORDER BY p.createdAt DESC")
    List<Product> findFlashSaleProducts();

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchActiveProducts(@Param("search") String search, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.category.id = :categoryId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND p.category.id = :categoryId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND p.category.id = :categoryId")
    Page<Product> searchActiveProductsByCategory(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true")
    Page<Product> findAllActive(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.isNew = true",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND p.isNew = true")
    Page<Product> findAllActiveNew(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.discountPercentage > 0",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND p.discountPercentage > 0")
    Page<Product> findAllActiveOnSale(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.isNew = true AND p.category.id = :categoryId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND p.isNew = true AND p.category.id = :categoryId")
    Page<Product> findActiveNewByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.active = true AND p.discountPercentage > 0 AND p.category.id = :categoryId",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Product p WHERE p.active = true AND p.discountPercentage > 0 AND p.category.id = :categoryId")
    Page<Product> findActiveOnSaleByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    long countByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.totalStock <= :threshold AND p.active = true ORDER BY p.totalStock ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}