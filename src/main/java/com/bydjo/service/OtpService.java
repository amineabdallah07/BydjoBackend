package com.bydjo.service;

public interface OtpService {
    void sendOtp(String phone);
    boolean verifyOtp(String phone, String code);
    void cleanupExpiredOtps();
}
