package com.bydjo.repository;

import com.bydjo.entity.QrOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrOrderItemRepository extends JpaRepository<QrOrderItem, Long> {
    Optional<QrOrderItem> findByQrCode(String qrCode);
    List<QrOrderItem> findByOrderItemId(Long orderItemId);

    @Query("SELECT q FROM QrOrderItem q ORDER BY q.createdAt DESC")
    List<QrOrderItem> findAllByOrderByCreatedAtDesc();

    @Query("SELECT q FROM QrOrderItem q WHERE q.orderItemId IN " +
           "(SELECT oi.id FROM OrderItem oi WHERE oi.order.id IN " +
           "(SELECT o.id FROM Order o WHERE o.user.id = :userId AND o.status = 'DELIVERED')) " +
           "ORDER BY q.createdAt DESC")
    List<QrOrderItem> findByUserIdAndDeliveredOrder(@Param("userId") Long userId);

    @Query("SELECT q FROM QrOrderItem q WHERE q.qrCode = :qrCode AND q.orderItemId IN " +
           "(SELECT oi.id FROM OrderItem oi WHERE oi.order.id IN " +
           "(SELECT o.id FROM Order o WHERE o.user.id = :userId))")
    Optional<QrOrderItem> findByQrCodeAndUserId(@Param("qrCode") String qrCode, @Param("userId") Long userId);
}
