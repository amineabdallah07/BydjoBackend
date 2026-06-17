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
}
