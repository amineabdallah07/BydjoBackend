package com.bydjo.dtos.qr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrOrderItemDto {
    private Long id;
    private Long orderItemId;
    private String qrType;
    private String content;
    private String qrCode;
    private Long qrCodeId;
    private String orderNumber;
    private String productName;
    private String customerName;
    private LocalDateTime createdAt;
}
