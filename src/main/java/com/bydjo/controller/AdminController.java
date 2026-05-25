package com.bydjo.controller;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.coupon.CouponCreateDto;
import com.bydjo.entity.Coupon;
import com.bydjo.repository.CouponRepository;
import com.bydjo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final CouponRepository couponRepository;

    // ======= DASHBOARD STATS =======

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats()));
    }

    @GetMapping("/analytics/monthly")
    public ResponseEntity<ApiResponse<Object>> getMonthlyAnalytics(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getMonthlyAnalytics(months)));
    }

    @GetMapping("/analytics/best-products")
    public ResponseEntity<ApiResponse<Object>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getBestSellingProducts(limit)));
    }

    @GetMapping("/analytics/order-status")
    public ResponseEntity<ApiResponse<Object>> getOrderStatusStats() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getOrderStatusStats()));
    }

    // ======= CUSTOMERS MANAGEMENT =======

    @GetMapping("/customers")
    public ResponseEntity<PagedResponse<Map<String, Object>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getCustomers(page, size, search));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerDetail(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getCustomerDetail(id)));
    }

    @PatchMapping("/customers/{id}/toggle-active")
    public ResponseEntity<ApiResponse<String>> toggleCustomerActive(@PathVariable Long id) {
        adminService.toggleCustomerActive(id);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour"));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(@PathVariable Long id) {
        adminService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Client supprimé avec succès"));
    }

    // ======= INVENTORY ALERTS =======

    @GetMapping("/inventory/alerts")
    public ResponseEntity<ApiResponse<Object>> getInventoryAlerts(
            @RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getLowStockAlerts(threshold)));
    }

    // ======= COUPONS MANAGEMENT =======

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<Object>> getCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponRepository.findAll()));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody CouponCreateDto dto) {
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().toUpperCase().trim())
                .description(dto.getDescription())
                .discountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : 0)
                .usageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 100)
                .startsAt(dto.getStartsAt())
                .expiresAt(dto.getExpiresAt())
                .active(true)
                .build();
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.success("Coupon créé avec succès", saved));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(@PathVariable Long id, @RequestBody CouponCreateDto dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : coupon.getDiscountPercentage());
        coupon.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : coupon.getUsageLimit());
        coupon.setStartsAt(dto.getStartsAt());
        coupon.setExpiresAt(dto.getExpiresAt());
        if (dto.getActive() != null) coupon.setActive(dto.getActive());
        Coupon saved = couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.success("Coupon mis à jour", saved));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon supprimé"));
    }
}