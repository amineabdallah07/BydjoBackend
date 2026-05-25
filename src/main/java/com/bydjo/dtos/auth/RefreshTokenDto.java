package com.bydjo.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDto {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
