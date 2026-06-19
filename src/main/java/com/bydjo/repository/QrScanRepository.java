package com.bydjo.repository;

import com.bydjo.entity.QrScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface QrScanRepository extends JpaRepository<QrScan, Long> {
    int countByQrCode(String qrCode);

    @Query("SELECT COUNT(s) FROM QrScan s WHERE s.qrCode = :qrCode AND s.scannedAt >= :since")
    int countByQrCodeSince(@Param("qrCode") String qrCode, @Param("since") LocalDateTime since);
}
