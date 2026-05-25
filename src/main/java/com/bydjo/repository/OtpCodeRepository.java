package com.bydjo.repository;

import com.bydjo.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);
    void deleteByPhone(String phone);

    // FIX Bug #10: Efficient bulk delete instead of findAll() + loop
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now AND o.verified = false")
    int deleteExpiredOtps(LocalDateTime now);
}
