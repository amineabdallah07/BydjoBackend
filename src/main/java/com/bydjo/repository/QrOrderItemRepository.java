package com.bydjo.repository;

import com.bydjo.entity.QrOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrOrderItemRepository extends JpaRepository<QrOrderItem, Long> {
    Optional<QrOrderItem> findByQrCode(String qrCode);
    List<QrOrderItem> findByOrderItemId(Long orderItemId);

    @Query("SELECT q FROM QrOrderItem q ORDER BY q.createdAt DESC")
    List<QrOrderItem> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM QrOrderItem q " +
           "JOIN OrderItem oi ON oi.id = q.orderItemId " +
           "JOIN Order o ON o.id = oi.order.id " +
           "WHERE o.user.id = :userId AND o.status = 'DELIVERED' " +
           "ORDER BY q.createdAt DESC")
    List<QrOrderItem> findByUserIdAndDeliveredOrder(@Param("userId") Long userId);

    @Query("SELECT q FROM QrOrderItem q " +
           "JOIN OrderItem oi ON oi.id = q.orderItemId " +
           "JOIN Order o ON o.id = oi.order.id " +
           "WHERE q.qrCode = :qrCode AND o.user.id = :userId")
    Optional<QrOrderItem> findByQrCodeAndUserId(@Param("qrCode") String qrCode, @Param("userId") Long userId);
}
