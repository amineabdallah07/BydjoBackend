package com.bydjo.service;

import com.bydjo.dtos.auth.*;
import com.bydjo.dtos.common.ApiResponse;

public interface AuthService {
    ApiResponse<String> sendOtp(OtpRequestDto request);
    ApiResponse<AuthResponseDto> verifyOtpAndLogin(OtpVerifyDto request);
    ApiResponse<AuthResponseDto> registerWithOtp(RegisterDto request, OtpVerifyDto otpRequest);
    ApiResponse<AuthResponseDto> refreshToken(RefreshTokenDto request);
    ApiResponse<UserDto> getCurrentUser(Long userId);
    ApiResponse<UserDto> updateProfile(Long userId, RegisterDto updateDto);
}
