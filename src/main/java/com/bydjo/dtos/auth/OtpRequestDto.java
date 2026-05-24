package com.bydjo.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpRequestDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+216[0-9]{8}$", message = "Invalid Tunisian phone number format (+216XXXXXXXX)")
    private String phone;
}
