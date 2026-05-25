package com.bydjo.service.impl;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.dtos.order.CreateOrderDto;
import com.bydjo.dtos.order.OrderDto;
import com.bydjo.dtos.order.OrderItemDto;
import com.bydjo.entity.*;
import com.bydjo.enums.OrderStatus;
import com.bydjo.enums.PaymentMethod;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.*;
import com.bydjo.service.OrderService;
import com.bydjo.service.impl.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    // FIX L4: Delivery fees from DB settings
    private final AppSettingService appSettingService;

    @Override
    @Transactional
    public OrderDto createOrder(Long userId, String sessionId, CreateOrderDto dto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        BigDecimal subtotal = cart.getSubtotal();
        // FIX Bug #4: Calculate real coupon discount
        BigDecimal discount = calculateCouponDiscount(dto.getCouponCode(), subtotal);
        BigDecimal shippingCost = calculateShipping(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(shippingCost);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .customerPhone(user.getPhone())
                .shippingFullName(dto.getShippingFullName())
                .shippingPhone(dto.getShippingPhone())
                .shippingGovernorate(dto.getShippingGovernorate())
                .shippingCity(dto.getShippingCity())
                .shippingAddress(dto.getShippingAddress())
                .shippingNotes(dto.getShippingNotes())
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .subtotal(subtotal)
                .discount(discount)
                .shippingCost(shippingCost)
                .total(total)
                .couponCode(dto.getCouponCode())
                .notes(dto.getNotes())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .variant(cartItem.getVariant())
                    .productName(cartItem.getProduct().getName())
                    .sizeName(cartItem.getVariant() != null && cartItem.getVariant().getSize() != null ?
                            cartItem.getVariant().getSize().getName() : null)
                    .colorName(cartItem.getVariant() != null && cartItem.getVariant().getColor() != null ?
                            cartItem.getVariant().getColor().getName() : null)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .productImage(cartItem.getProduct().getImages().stream()
                            .filter(ProductImage::getIsPrimary).findFirst()
                            .map(ProductImage::getUrl).orElse(null))
                    .build();

            order.getItems().add(orderItem);

            // Update stock
            if (cartItem.getVariant() != null) {
                cartItem.getVariant().setStock(
                        Math.max(0, cartItem.getVariant().getStock() - cartItem.getQuantity()));
            }
            cartItem.getProduct().setTotalSold(
                    cartItem.getProduct().getTotalSold() + cartItem.getQuantity());
        }

        order = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDto(order);
    }

    @Override
    @Transactional
    public OrderDto createGuestOrder(CreateOrderDto dto) {
        // Create order from session cart
        Cart cart = cartRepository.findBySessionId(dto.getSessionCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal subtotal = cart.getSubtotal();
        // FIX Bug #4: Calculate real coupon discount for guest orders
        BigDecimal discount = calculateCouponDiscount(dto.getCouponCode(), subtotal);
        BigDecimal shippingCost = calculateShipping(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(shippingCost);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .guestName(dto.getShippingFullName())
                .guestPhone(dto.getShippingPhone())
                .guestEmail(dto.getEmail())
                .customerPhone(dto.getShippingPhone())
                .shippingFullName(dto.getShippingFullName())
                .shippingPhone(dto.getShippingPhone())
                .shippingGovernorate(dto.getShippingGovernorate())
                .shippingCity(dto.getShippingCity())
                .shippingAddress(dto.getShippingAddress())
                .shippingNotes(dto.getShippingNotes())
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .subtotal(subtotal)
                .discount(discount)
                .shippingCost(shippingCost)
                .total(total)
                .couponCode(dto.getCouponCode())
                .notes(dto.getNotes())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .variant(cartItem.getVariant())
                    .productName(cartItem.getProduct().getName())
                    .sizeName(cartItem.getVariant() != null && cartItem.getVariant().getSize() != null ?
                            cartItem.getVariant().getSize().getName() : null)
                    .colorName(cartItem.getVariant() != null && cartItem.getVariant().getColor() != null ?
                            cartItem.getVariant().getColor().getName() : null)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .productImage(cartItem.getProduct().getImages().stream()
                            .filter(ProductImage::getIsPrimary).findFirst()
                            .map(ProductImage::getUrl).orElse(null))
                    .build();

            order.getItems().add(orderItem);

            // FIX Bug #1 & #2: Decrement stock and update totalSold for guest orders
            if (cartItem.getVariant() != null) {
                cartItem.getVariant().setStock(
                        Math.max(0, cartItem.getVariant().getStock() - cartItem.getQuantity()));
            }
            cartItem.getProduct().setTotalSold(
                    cartItem.getProduct().getTotalSold() + cartItem.getQuantity());
        }

        order = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDto(order);
    }

    @Override
    public PagedResponse<OrderDto> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return mapToPagedResponse(orders);
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToDto(order);
    }

    @Override
    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapToDto(order);
    }

    @Override
    public PagedResponse<OrderDto> getAllOrders(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapToPagedResponse(orders);
    }

    @Override
    public PagedResponse<OrderDto> getOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return mapToPagedResponse(orders);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        // Charge la commande avec ses items/variants/produits en JOIN FETCH
        // pour garantir la restitution du stock même avec une session courte
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        order.setStatus(status);

        switch (status) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> {
                order.setCancelledAt(LocalDateTime.now());
                // FIX L5: Reintegrate stock when order is cancelled
                for (OrderItem item : order.getItems()) {
                    if (item.getVariant() != null) {
                        item.getVariant().setStock(item.getVariant().getStock() + item.getQuantity());
                    }
                    if (item.getProduct() != null) {
                        item.getProduct().setTotalSold(
                                Math.max(0, item.getProduct().getTotalSold() - item.getQuantity()));
                    }
                }
            }
            default -> {}
        }

        order = orderRepository.save(order);
        return mapToDto(order);
    }

    @Override
    public PagedResponse<OrderDto> searchOrders(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.searchOrders(query, pageable);
        return mapToPagedResponse(orders);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Vérifier que la commande appartient au user
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à annuler cette commande");
        }

        // Seules les commandes PENDING peuvent être annulées par le user
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Seules les commandes en attente peuvent être annulées");
        }

        // Restituer le stock
        for (OrderItem item : order.getItems()) {
            if (item.getVariant() != null) {
                item.getVariant().setStock(item.getVariant().getStock() + item.getQuantity());
                Product p = item.getVariant().getProduct();
                if (p != null) {
                    p.setTotalStock(p.getTotalStock() + item.getQuantity());
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        return mapToDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        orderRepository.delete(order);
    }

    @Override
    public long getOrderCount() {
        return orderRepository.count();
    }

    @Override
    public Double getTotalRevenue() {
        return orderRepository.calculateRevenueBetween(
                LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.now());
    }

    private BigDecimal calculateShipping(BigDecimal subtotalAfterDiscount) {
        // FIX L4: Fees from DB — not hardcoded
        BigDecimal freeThreshold = appSettingService.getDecimal("delivery.free_threshold", BigDecimal.valueOf(150));
        BigDecimal fee = appSettingService.getDecimal("delivery.fee", BigDecimal.valueOf(8));
        return subtotalAfterDiscount.compareTo(freeThreshold) >= 0 ? BigDecimal.ZERO : fee;
    }

    private BigDecimal calculateCouponDiscount(String couponCode, BigDecimal subtotal) {
        if (couponCode == null || couponCode.isBlank()) return BigDecimal.ZERO;
        return couponRepository.findByCode(couponCode)
                .filter(c -> c.isValid())
                .map(coupon -> {
                    if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                        return BigDecimal.ZERO;
                    }
                    BigDecimal d = BigDecimal.ZERO;
                    if (coupon.getDiscountPercentage() != null && coupon.getDiscountPercentage() > 0) {
                        d = subtotal.multiply(BigDecimal.valueOf(coupon.getDiscountPercentage()))
                                .divide(BigDecimal.valueOf(100));
                        if (coupon.getMaxDiscountAmount() != null) {
                            d = d.min(coupon.getMaxDiscountAmount());
                        }
                    } else if (coupon.getDiscountAmount() != null) {
                        d = coupon.getDiscountAmount().min(subtotal);
                    }
                    // Increment usedCount
                    coupon.setUsedCount(coupon.getUsedCount() + 1);
                    couponRepository.save(coupon);
                    return d;
                })
                .orElse(BigDecimal.ZERO);
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // FIX Bug #3: Use UUID to eliminate collision risk (was Random().nextInt(10000) = only 10k combos/day)
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "BDJ-" + date + "-" + unique;
    }

    private OrderDto mapToDto(Order o) {
        return OrderDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .userId(o.getUser() != null ? o.getUser().getId() : null)
                .customerName(o.getUser() != null ? o.getUser().getFullName() : o.getGuestName())
                .customerPhone(o.getCustomerPhone())
                .customerEmail(o.getUser() != null ? o.getUser().getEmail() : o.getGuestEmail())
                .shippingFullName(o.getShippingFullName())
                .shippingPhone(o.getShippingPhone())
                .shippingGovernorate(o.getShippingGovernorate())
                .shippingCity(o.getShippingCity())
                .shippingAddress(o.getShippingAddress())
                .shippingNotes(o.getShippingNotes())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .subtotal(o.getSubtotal())
                .discount(o.getDiscount())
                .shippingCost(o.getShippingCost())
                .total(o.getTotal())
                .couponCode(o.getCouponCode())
                .notes(o.getNotes())
                .items(o.getItems().stream().map(this::mapItemToDto).collect(Collectors.toList()))
                .isGuest(o.isGuestOrder())
                .createdAt(o.getCreatedAt())
                .confirmedAt(o.getConfirmedAt())
                .shippedAt(o.getShippedAt())
                .deliveredAt(o.getDeliveredAt())
                .build();
    }

    private OrderItemDto mapItemToDto(OrderItem i) {
        return OrderItemDto.builder()
                .id(i.getId())
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .sizeName(i.getSizeName())
                .colorName(i.getColorName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .productImage(i.getProductImage())
                .build();
    }

    private PagedResponse<OrderDto> mapToPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderDto>builder()
                .content(page.getContent().stream().map(this::mapToDto).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
