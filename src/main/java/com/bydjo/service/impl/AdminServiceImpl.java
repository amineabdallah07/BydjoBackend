package com.bydjo.service.impl;

import com.bydjo.dtos.common.PagedResponse;
import com.bydjo.entity.User;
import com.bydjo.enums.OrderStatus;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.*;
import com.bydjo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final AddressRepository addressRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long preparingOrders = orderRepository.countByStatus(OrderStatus.PREPARING);
        long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);

        Double totalRevenue = orderRepository.calculateRevenueBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), now);
        Double monthRevenue = orderRepository.calculateRevenueBetween(startOfMonth, now);
        Double lastMonthRevenue = orderRepository.calculateRevenueBetween(startOfLastMonth, startOfMonth);

        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByActiveTrue();
        long totalCustomers = userRepository.count();
        long monthOrders = orderRepository.countOrdersBetween(startOfMonth, now);
        long lastMonthOrders = orderRepository.countOrdersBetween(startOfLastMonth, startOfMonth);

        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("confirmedOrders", confirmedOrders);
        stats.put("preparingOrders", preparingOrders);
        stats.put("shippedOrders", shippedOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("monthRevenue", monthRevenue != null ? monthRevenue : 0.0);
        stats.put("lastMonthRevenue", lastMonthRevenue != null ? lastMonthRevenue : 0.0);
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("totalCustomers", totalCustomers);
        stats.put("monthOrders", monthOrders);
        stats.put("lastMonthOrders", lastMonthOrders);

        // Revenue growth percentage
        double revenueGrowth = 0;
        if (lastMonthRevenue != null && lastMonthRevenue > 0 && monthRevenue != null) {
            revenueGrowth = ((monthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
        }
        stats.put("revenueGrowth", Math.round(revenueGrowth * 10.0) / 10.0);

        // Orders growth
        double ordersGrowth = 0;
        if (lastMonthOrders > 0) {
            ordersGrowth = ((double)(monthOrders - lastMonthOrders) / lastMonthOrders) * 100;
        }
        stats.put("ordersGrowth", Math.round(ordersGrowth * 10.0) / 10.0);

        return stats;
    }

    @Override
    public Object getMonthlyAnalytics(int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1);

            Double revenue = orderRepository.calculateRevenueBetween(start, end);
            long orders = orderRepository.countOrdersBetween(start, end);

            Map<String, Object> month = new LinkedHashMap<>();
            month.put("month", start.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            month.put("monthKey", start.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            month.put("revenue", revenue != null ? Math.round(revenue * 1000.0) / 1000.0 : 0.0);
            month.put("orders", orders);
            result.add(month);
        }
        return result;
    }

    @Override
    public Object getBestSellingProducts(int limit) {
        return productRepository.findBestSellers(PageRequest.of(0, limit))
                .stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("slug", p.getSlug());
                    m.put("totalSold", p.getTotalSold());
                    m.put("price", p.getPrice());
                    m.put("totalStock", p.getTotalStock());
                    m.put("imageUrl", p.getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                            .findFirst()
                            .map(img -> img.getUrl())
                            .orElse(p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl()));
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Object getOrderStatusStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", status.name());
            m.put("count", orderRepository.countByStatus(status));
            result.add(m);
        }
        return result;
    }

    @Override
    public PagedResponse<Map<String, Object>> getCustomers(int page, int size, String search) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> usersPage;
        if (search != null && !search.isBlank()) {
            usersPage = userRepository.searchCustomers(search.trim(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        List<Map<String, Object>> content = usersPage.getContent().stream()
                .map(u -> buildCustomerMap(u, false))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast(),
                usersPage.isFirst()
        );
    }

    @Override
    public Map<String, Object> getCustomerDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return buildCustomerMap(user, true);
    }

    @Override
    @Transactional
    public void toggleCustomerActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(!user.getActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Dissocier les commandes (on conserve l'historique, on retire juste le lien)
        orderRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent()
                .forEach(order -> {
                    order.setUser(null);
                    orderRepository.save(order);
                });

        // 2. Supprimer le panier (cascade supprime les CartItems)
        cartRepository.deleteByUserId(userId);

        // 3. Supprimer la wishlist
        wishlistRepository.deleteByUserId(userId);

        // 4. Supprimer les adresses
        addressRepository.deleteByUserId(userId);

        // 5. Supprimer les notifications
        notificationRepository.deleteByUserId(userId);

        // 6. Supprimer les avis
        reviewRepository.deleteByUserId(userId);

        // 7. Supprimer le user (la table user_roles est gérée par JPA cascade)
        userRepository.delete(user);
    }

    @Override
    public Object getLowStockAlerts(int threshold) {
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("sku", p.getSku());
                    m.put("totalStock", p.getTotalStock());
                    m.put("price", p.getPrice());
                    m.put("imageUrl", p.getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                            .findFirst()
                            .map(img -> img.getUrl())
                            .orElse(null));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildCustomerMap(User u, boolean withOrders) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("firstName", u.getFirstName());
        m.put("lastName", u.getLastName());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("active", u.getActive());
        m.put("phoneVerified", u.getPhoneVerified());
        m.put("createdAt", u.getCreatedAt());
        m.put("roles", u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()));
        if (withOrders) {
            long orderCount = orderRepository.countByUserId(u.getId());
            Double totalSpent = orderRepository.sumTotalByUserId(u.getId());
            m.put("orderCount", orderCount);
            m.put("totalSpent", totalSpent != null ? totalSpent : 0.0);
        }
        return m;
    }
}