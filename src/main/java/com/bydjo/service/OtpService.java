package com.bydjo.service;

public interface OtpService {
    String sendOtp(String phone);
    boolean verifyOtp(String phone, String code);
    void cleanupExpiredOtps();
}
