package com.bydjo.service.impl;

import com.bydjo.dtos.auth.*;
import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.entity.Role;
import com.bydjo.entity.User;
import com.bydjo.enums.RoleName;
import com.bydjo.repository.RoleRepository;
import com.bydjo.repository.UserRepository;
import com.bydjo.security.JwtTokenProvider;
import com.bydjo.security.UserPrincipal;
import com.bydjo.service.AuthService;
import com.bydjo.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpService otpService;
    private final FirebaseService firebaseService;
    private final JwtTokenProvider tokenProvider;

    @Override
    public ApiResponse<String> sendOtp(OtpRequestDto request) {
        String code = otpService.sendOtp(request.getPhone());
        return ApiResponse.success("OTP sent successfully to " + request.getPhone(), code);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> verifyOtpAndLogin(OtpVerifyDto request) {
        boolean verified = otpService.verifyOtp(request.getPhone(), request.getCode());

        if (!verified) {
            return ApiResponse.error("Invalid or expired OTP code");
        }

        // Find or create user
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    // Auto-register on first login
                    User newUser = User.builder()
                            .phone(request.getPhone())
                            .firstName("")
                            .lastName("")
                            .phoneVerified(true)
                            .active(true)
                            .roles(new HashSet<>())
                            .build();

                    Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                            .orElseThrow(() -> new RuntimeException("Customer role not found"));
                    newUser.getRoles().add(customerRole);

                    return userRepository.save(newUser);
                });

        if (!user.getPhoneVerified()) {
            user.setPhoneVerified(true);
            userRepository.save(user);
        }

        // Generate JWT
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        AuthResponseDto response = new AuthResponseDto(
                accessToken,
                refreshToken,
                tokenProvider.getExpirationMillis(),
                mapToUserDto(user)
        );

        return ApiResponse.success("Login successful", response);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> firebaseLogin(String idToken) {
        String phone = firebaseService.verifyAndGetPhone(idToken);

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .phone(phone)
                            .firstName("")
                            .lastName("")
                            .phoneVerified(true)
                            .active(true)
                            .roles(new HashSet<>())
                            .build();

                    Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                            .orElseThrow(() -> new RuntimeException("Customer role not found"));
                    newUser.getRoles().add(customerRole);

                    return userRepository.save(newUser);
                });

        if (!user.getPhoneVerified()) {
            user.setPhoneVerified(true);
            userRepository.save(user);
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        AuthResponseDto response = new AuthResponseDto(
                accessToken, refreshToken,
                tokenProvider.getExpirationMillis(),
                mapToUserDto(user)
        );

        return ApiResponse.success("Login successful", response);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> registerWithOtp(RegisterDto request, OtpVerifyDto otpRequest) {
        if (userRepository.existsByPhone(request.getPhone())) {
            return ApiResponse.error("Phone number already registered");
        }

        boolean verified = otpService.verifyOtp(otpRequest.getPhone(), otpRequest.getCode());
        if (!verified) {
            return ApiResponse.error("Invalid or expired OTP code");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .phoneVerified(true)
                .active(true)
                .roles(new HashSet<>())
                .build();

        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.getRoles().add(customerRole);

        user = userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        AuthResponseDto response = new AuthResponseDto(
                accessToken, refreshToken,
                tokenProvider.getExpirationMillis(),
                mapToUserDto(user)
        );

        return ApiResponse.success("Registration successful", response);
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> refreshToken(RefreshTokenDto request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            return ApiResponse.error("Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        AuthResponseDto response = new AuthResponseDto(
                newAccessToken, newRefreshToken,
                tokenProvider.getExpirationMillis(),
                mapToUserDto(user)
        );

        return ApiResponse.success("Token refreshed", response);
    }

    @Override
    @Transactional
    public ApiResponse<UserDto> getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ApiResponse.success(mapToUserDto(user));
    }

    @Override
    @Transactional
    public ApiResponse<UserDto> updateProfile(Long userId, RegisterDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());

        user = userRepository.save(user);
        return ApiResponse.success("Profile updated", mapToUserDto(user));
    }

    private UserDto mapToUserDto(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .phoneVerified(user.getPhoneVerified())
                .roles(roles)
                .build();
    }
}
