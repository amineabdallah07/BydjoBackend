package com.bydjo.service;

import com.bydjo.dtos.common.PagedResponse;

import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboardStats();
    Object getMonthlyAnalytics(int months);
    Object getBestSellingProducts(int limit);
    Object getOrderStatusStats();
    PagedResponse<Map<String, Object>> getCustomers(int page, int size, String search);
    Map<String, Object> getCustomerDetail(Long userId);
    void toggleCustomerActive(Long userId);
    void deleteCustomer(Long userId);
    Object getLowStockAlerts(int threshold);
}