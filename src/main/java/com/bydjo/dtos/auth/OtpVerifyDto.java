package com.bydjo.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+216[0-9]{8}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String code;
}
