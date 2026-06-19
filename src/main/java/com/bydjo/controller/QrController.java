package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.qr.QrCodeDto;
import com.bydjo.dtos.qr.QrOrderItemDto;
import com.bydjo.service.FileStorageService;
import com.bydjo.service.QrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
@Tag(name = "QR Codes", description = "QR code content and admin management")
public class QrController {

    private final QrService qrService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a photo for a QR product (public)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPhoto(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.storeFile(file, "qr-photos");
        return ResponseEntity.ok(ApiResponse.success("Photo uploaded", Map.of("url", url)));
    }

    @GetMapping("/r/{qrCode}")
    @Operation(summary = "Public redirect endpoint: 302 to customer link for LINK type, or to Angular for photo")
    public ResponseEntity<Void> redirectQr(@PathVariable String qrCode) {
        String url = qrService.getRedirectUrl(qrCode);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", url);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/{qrCode}")
    @Operation(summary = "Public endpoint: get QR code content (photo URL or redirect link)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQrContent(@PathVariable String qrCode) {
        return ResponseEntity.ok(qrService.getQrContent(qrCode));
    }

    @GetMapping("/my-tshirts")
    @Operation(summary = "Get my QR t-shirts from delivered orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<QrOrderItemDto>>> getMyQrShirts(
            @AuthenticationPrincipal com.bydjo.security.UserPrincipal user) {
        return ResponseEntity.ok(qrService.getMyQrShirts(user.getId()));
    }

    @PutMapping("/my-tshirts/{qrCode}/content")
    @Operation(summary = "Update my QR t-shirt content (link or photo URL)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateMyQrContent(
            @PathVariable String qrCode,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal com.bydjo.security.UserPrincipal user) {
        String newContent = body.get("content");
        if (newContent == null || newContent.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Content is required"));
        }
        return ResponseEntity.ok(qrService.updateMyQrContent(qrCode, newContent, user.getId()));
    }

    @GetMapping("/orders")
    @Operation(summary = "Get all QR order items (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QrOrderItemDto>>> getAllQrOrders() {
        return ResponseEntity.ok(qrService.getAllQrOrderItems());
    }

    @PostMapping("/codes/generate")
    @Operation(summary = "Generate empty QR codes (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QrCodeDto>>> generateQrCodes(
            @RequestParam(defaultValue = "50") int count,
            @RequestParam(defaultValue = "") String size) {
        if (count < 1) count = 1;
        if (count > 1000) count = 1000;
        String sz = (size == null || size.isBlank()) ? null : size.toUpperCase();
        List<QrCodeDto> codes = qrService.generateQrCodes(count, sz);
        return ResponseEntity.ok(ApiResponse.success("Generated " + count + " QR codes (size=" + sz + ")", codes));
    }

    @GetMapping("/codes")
    @Operation(summary = "Get all QR codes with status (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<QrCodeDto>>> getAllQrCodes() {
        return ResponseEntity.ok(ApiResponse.success(qrService.getAllQrCodes()));
    }

    @GetMapping("/codes/stats")
    @Operation(summary = "Get QR code stats (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQrCodeStats() {
        return ResponseEntity.ok(qrService.getQrCodeStats());
    }

    @DeleteMapping("/codes/{id}")
    @Operation(summary = "Delete a free QR code (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQrCode(@PathVariable Long id) {
        qrService.deleteQrCode(id);
        return ResponseEntity.ok(ApiResponse.success("QR code deleted"));
    }
}
