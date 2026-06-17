package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.tshirt.TshirtDto;
import com.bydjo.security.UserPrincipal;
import com.bydjo.service.TshirtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tshirts")
@RequiredArgsConstructor
@Tag(name = "T-shirts", description = "QR code T-shirt scan tracking")
public class TshirtController {

    private final TshirtService tshirtService;

    @PostMapping
    @Operation(summary = "Create a new T-shirt (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TshirtDto>> createTshirt(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Code is required"));
        }
        return ResponseEntity.ok(tshirtService.createTshirt(code));
    }

    @PutMapping("/{code}/assign")
    @Operation(summary = "Assign T-shirt to a user (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TshirtDto>> assignTshirt(@PathVariable String code, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("userId is required"));
        }
        return ResponseEntity.ok(tshirtService.assignTshirt(code, userId));
    }

    @GetMapping("/scan/{code}")
    @Operation(summary = "Public endpoint: register a scan when QR code is scanned")
    public ResponseEntity<ApiResponse<TshirtDto>> scanTshirt(@PathVariable String code, HttpServletRequest request) {
        return ResponseEntity.ok(tshirtService.registerScan(code, request));
    }

    @GetMapping("/mine")
    @Operation(summary = "Get my T-shirts with scan counts")
    public ResponseEntity<ApiResponse<List<TshirtDto>>> getMyTshirts(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(tshirtService.getMyTshirts(user.getId()));
    }

    @GetMapping
    @Operation(summary = "Get all T-shirts (Admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TshirtDto>>> getAllTshirts() {
        return ResponseEntity.ok(tshirtService.getAllTshirts());
    }
}
