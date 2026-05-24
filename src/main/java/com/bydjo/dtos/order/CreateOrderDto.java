package com.bydjo.dtos.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDto {
    @NotBlank(message = "Full name is required")
    private String shippingFullName;

    @NotBlank(message = "Phone number is required")
    private String shippingPhone;

    private String email;

    @NotBlank(message = "Governorate is required")
    private String shippingGovernorate;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "Address is required")
    private String shippingAddress;

    private String shippingNotes;

    private String couponCode;

    private String notes;

    private String sessionCartId;
}
