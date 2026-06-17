package com.bydjo.repository;

import com.bydjo.entity.TshirtScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TshirtScanRepository extends JpaRepository<TshirtScan, Long> {
    List<TshirtScan> findByTshirtCodeOrderByScannedAtDesc(String tshirtCode);
    int countByTshirtCode(String tshirtCode);
}
