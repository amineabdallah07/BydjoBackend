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
public class QrCodeDto {
    private Long id;
    private String code;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private String customerName;
    private String productName;
    private String orderNumber;
    private String qrType;
    private String content;
}
