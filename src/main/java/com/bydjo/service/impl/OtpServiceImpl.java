package com.bydjo.service.impl;

import com.bydjo.entity.OtpCode;
import com.bydjo.repository.OtpCodeRepository;
import com.bydjo.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final SmsService smsService;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public String sendOtp(String phone) {
        var existingOtp = otpCodeRepository.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone);
        if (existingOtp.isPresent()) {
            OtpCode otp = existingOtp.get();
            if (otp.getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Please wait 60 seconds before requesting a new OTP");
            }
            if (otp.getAttempts() >= maxAttempts) {
                throw new RuntimeException("Maximum OTP attempts exceeded. Please try again later.");
            }
        }

        String code = generateOtpCode();

        OtpCode otpCode = OtpCode.builder()
                .phone(phone)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .verified(false)
                .attempts(0)
                .build();

        otpCodeRepository.save(otpCode);

        String message = String.format("BY DJO - Your verification code is: %s. Valid for %d minutes.", code, expirationMinutes);
        smsService.sendSms(phone, message);
        return code;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String phone, String code) {
        var otpOptional = otpCodeRepository.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(phone);

        if (otpOptional.isEmpty()) {
            log.warn("No OTP found for phone: {}", phone);
            return false;
        }

        OtpCode otpCode = otpOptional.get();

        // Increment attempts
        otpCode.setAttempts(otpCode.getAttempts() + 1);
        otpCodeRepository.save(otpCode);

        if (otpCode.getAttempts() > maxAttempts) {
            log.warn("Max attempts exceeded for phone: {}", phone);
            return false;
        }

        if (otpCode.isExpired()) {
            log.warn("OTP expired for phone: {}", phone);
            return false;
        }

        if (!otpCode.getCode().equals(code)) {
            log.warn("Invalid OTP for phone: {}", phone);
            return false;
        }

        // Mark as verified
        otpCode.setVerified(true);
        otpCodeRepository.save(otpCode);

        // Clean up old OTPs for this phone
        otpCodeRepository.deleteByPhone(phone);

        return true;
    }

    @Override
    @Transactional
    @Async
    public void cleanupExpiredOtps() {
        // FIX Bug #10: Single DELETE query instead of loading all OTPs into memory
        int deleted = otpCodeRepository.deleteExpiredOtps(LocalDateTime.now());
        log.info("Cleaned up {} expired OTP codes", deleted);
    }

    private String generateOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
