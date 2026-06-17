package com.bydjo.dtos.qr;

import lombok.Data;

@Data
public class QrDataDto {
    private Long productId;
    private String qrType;
    private String content;
}
