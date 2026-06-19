package com.bydjo.service;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.qr.QrCodeDto;
import com.bydjo.dtos.qr.QrOrderItemDto;

import java.util.List;
import java.util.Map;

public interface QrService {
    QrOrderItemDto createQrOrderItem(Long orderItemId, String qrType, String content, String size);
    ApiResponse<Map<String, Object>> getQrContent(String qrCode);
    ApiResponse<List<QrOrderItemDto>> getAllQrOrderItems();
    List<QrCodeDto> generateQrCodes(int count, String size);
    List<QrCodeDto> getAllQrCodes();
    ApiResponse<Map<String, Object>> getQrCodeStats();
    void deleteQrCode(Long id);
    String getRedirectUrl(String qrCode);
    ApiResponse<List<QrOrderItemDto>> getMyQrShirts(Long userId);
    ApiResponse<Void> updateMyQrContent(String qrCode, String newContent, Long userId);
    void registerScan(String qrCode, String ipAddress);
    ApiResponse<Map<String, Object>> getQrScanStats(String qrCode, Long userId);
}
