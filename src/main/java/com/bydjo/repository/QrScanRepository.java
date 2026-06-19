package com.bydjo.repository;

import com.bydjo.entity.QrScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrScanRepository extends JpaRepository<QrScan, Long> {
    int countByQrCode(String qrCode);

    @Query(value = "SELECT TO_CHAR(DATE(s.scanned_at), 'YYYY-MM-DD') as scan_date, COUNT(*)::int as cnt " +
           "FROM qr_scans s WHERE s.qr_code = :qrCode " +
           "GROUP BY DATE(s.scanned_at) " +
           "ORDER BY DATE(s.scanned_at) DESC", nativeQuery = true)
    List<Object[]> countByDay(@Param("qrCode") String qrCode);
}
