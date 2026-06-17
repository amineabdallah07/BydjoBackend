package com.bydjo.controller;

import com.bydjo.dtos.auth.*;
import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.security.TokenBlacklistService;
import com.bydjo.security.JwtTokenProvider;
import com.bydjo.security.UserPrincipal;
import com.bydjo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Phone OTP Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to phone number")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpRequestDto request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and login/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new account")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody RegisterDto registerDto,
            @RequestParam String otpCode) {
        OtpVerifyDto otpVerify = new OtpVerifyDto();
        otpVerify.setPhone(registerDto.getPhone());
        otpVerify.setCode(otpCode);
        return ResponseEntity.ok(authService.registerWithOtp(registerDto, otpVerify));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenDto request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(authService.getCurrentUser(userPrincipal.getId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RegisterDto updateDto) {
        return ResponseEntity.ok(authService.updateProfile(userPrincipal.getId(), updateDto));
    }

    // FIX Bug #13: Backend logout — blacklist the current token so it can no longer be used
    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate current token")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            long expiryMillis = System.currentTimeMillis() + jwtTokenProvider.getExpirationMillis();
            tokenBlacklistService.blacklist(token, expiryMillis);
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
