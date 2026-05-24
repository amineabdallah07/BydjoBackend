package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.service.impl.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * FIX Bug #14 + L4:
 *  - GET /store/info   — reads settings from DB (was reading static @Value)
 *  - PUT /store/settings (ADMIN) — persists settings to DB (was missing entirely)
 * Delivery fees now come from DB, not hardcoded constants.
 */
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final AppSettingService appSettingService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getStoreInfo() {
        return ResponseEntity.ok(appSettingService.getAllSettings());
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateSettings(@RequestBody Map<String, String> settings) {
        // Only allow known keys to be updated (whitelist)
        settings.keySet().retainAll(java.util.Set.of(
                "store.name", "store.phone", "store.email", "store.address",
                "store.whatsapp", "store.facebook", "store.instagram",
                "delivery.fee", "delivery.free_threshold"
        ));
        appSettingService.saveAll(settings);
        return ResponseEntity.ok(ApiResponse.success("Settings saved successfully", null));
    }
}
