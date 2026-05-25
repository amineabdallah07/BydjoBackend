package com.bydjo.dtos.order;

import com.bydjo.enums.OrderStatus;
import com.bydjo.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingFullName;
    private String shippingPhone;
    private String shippingGovernorate;
    private String shippingCity;
    private String shippingAddress;
    private String shippingNotes;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String couponCode;
    private String notes;
    private List<OrderItemDto> items;
    private Boolean isGuest;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
