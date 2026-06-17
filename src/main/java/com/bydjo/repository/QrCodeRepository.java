package com.bydjo.repository;

import com.bydjo.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    List<QrCode> findByStatus(String status);
    Optional<QrCode> findFirstByStatus(String status);
    long countByStatus(String status);
    List<QrCode> findAllByOrderByCreatedAtDesc();
    Optional<QrCode> findByCode(String code);
}
