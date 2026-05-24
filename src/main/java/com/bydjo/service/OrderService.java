package com.bydjo.service;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.order.CreateOrderDto;
import com.bydjo.dtos.order.OrderDto;
import com.bydjo.enums.OrderStatus;

public interface OrderService {
    OrderDto createOrder(Long userId, String sessionId, CreateOrderDto dto);
    OrderDto createGuestOrder(CreateOrderDto dto);
    PagedResponse<OrderDto> getUserOrders(Long userId, int page, int size);
    OrderDto getOrderById(Long id);
    OrderDto getOrderByNumber(String orderNumber);
    PagedResponse<OrderDto> getAllOrders(int page, int size, String sort);
    PagedResponse<OrderDto> getOrdersByStatus(OrderStatus status, int page, int size);
    OrderDto updateOrderStatus(Long id, OrderStatus status);
    PagedResponse<OrderDto> searchOrders(String query, int page, int size);
    OrderDto cancelOrder(Long orderId, Long userId);
    void deleteOrder(Long id);
    long getOrderCount();
    Double getTotalRevenue();
}