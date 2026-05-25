package com.bydjo.service.impl;

import com.bydjo.dtos.cart.AddToCartDto;
import com.bydjo.dtos.cart.CartDto;
import com.bydjo.dtos.cart.CartItemDto;
import com.bydjo.entity.*;
import com.bydjo.exceptions.ResourceNotFoundException;
import com.bydjo.repository.*;
import com.bydjo.service.CartService;
import com.bydjo.service.impl.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CouponRepository couponRepository;
    // FIX L4: Delivery fees come from DB settings, not hardcoded constants
    private final AppSettingService appSettingService;

    @Override
    @Transactional
    public CartDto getCart(Long userId, String sessionId) {
        Cart cart = findOrCreateCart(userId, sessionId);
        return mapToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addToCart(Long userId, String sessionId, AddToCartDto dto) {
        Cart cart = findOrCreateCart(userId, sessionId);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", dto.getProductId()));

        ProductVariant variant = null;
        if (dto.getVariantId() != null) {
            variant = productVariantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", dto.getVariantId()));
        }

        // Vérifier si l'article est déjà dans le panier
        for (CartItem item : cart.getItems()) {
            boolean sameProduct = item.getProduct().getId().equals(product.getId());
            boolean sameVariant = (item.getVariant() == null && variant == null) ||
                    (item.getVariant() != null && variant != null &&
                            item.getVariant().getId().equals(variant.getId()));

            if (sameProduct && sameVariant) {
                item.setQuantity(item.getQuantity() + dto.getQuantity());
                cartItemRepository.save(item);
                // ✅ Re-fetch depuis la DB pour charger toutes les relations
                return mapToDto(findOrCreateCart(userId, sessionId));
            }
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(dto.getQuantity())
                .unitPrice(product.getPrice())
                .build();

        cart.getItems().add(cartItem);
        // ✅ saveAndFlush pour persister immédiatement sans déclencher @PreUpdate sur Product
        cartRepository.saveAndFlush(cart);

        // ✅ Re-fetch depuis la DB pour charger toutes les relations
        return mapToDto(findOrCreateCart(userId, sessionId));
    }

    @Override
    @Transactional
    public CartDto updateCartItem(Long userId, String sessionId, Long itemId, Integer quantity) {
        Cart cart = findCart(userId, sessionId);
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cartRepository.saveAndFlush(cart);
        // ✅ Re-fetch depuis la DB
        return mapToDto(findCart(userId, sessionId));
    }

    @Override
    @Transactional
    public CartDto removeFromCart(Long userId, String sessionId, Long itemId) {
        Cart cart = findCart(userId, sessionId);
        CartItem item = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", itemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cartRepository.saveAndFlush(cart);

        // ✅ Re-fetch depuis la DB
        return mapToDto(findCart(userId, sessionId));
    }

    @Override
    @Transactional
    public void clearCart(Long userId, String sessionId) {
        Cart cart = findCart(userId, sessionId);
        cart.getItems().clear();
        cart.setCouponCode(null);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartDto applyCoupon(Long userId, String sessionId, String couponCode) {
        Cart cart = findOrCreateCart(userId, sessionId);
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Code promo invalide"));

        if (!coupon.isValid()) {
            throw new RuntimeException("Ce code promo n'est plus valide");
        }

        cart.setCouponCode(couponCode);
        cartRepository.save(cart);
        return mapToDto(findOrCreateCart(userId, sessionId));
    }

    @Override
    @Transactional
    public CartDto removeCoupon(Long userId, String sessionId) {
        Cart cart = findOrCreateCart(userId, sessionId);
        cart.setCouponCode(null);
        cartRepository.save(cart);
        return mapToDto(findOrCreateCart(userId, sessionId));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Cart findOrCreateCart(Long userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Cart cart = Cart.builder()
                                .user(User.builder().id(userId).build())
                                .sessionId(UUID.randomUUID().toString())
                                .build();
                        return cartRepository.save(cart);
                    });
        }
        String sid = (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
        return cartRepository.findBySessionId(sid)
                .orElseGet(() -> cartRepository.save(Cart.builder().sessionId(sid).build()));
    }

    private Cart findCart(Long userId, String sessionId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        }
        return cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "sessionId", sessionId));
    }

    private CartDto mapToDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::mapItemToDto)
                .collect(Collectors.toList());

        BigDecimal subtotal = cart.getSubtotal();

        // FIX Bug #4: Calculate actual coupon discount
        BigDecimal discount = BigDecimal.ZERO;
        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank()) {
            discount = couponRepository.findByCode(cart.getCouponCode())
                    .filter(Coupon::isValid)
                    .map(coupon -> {
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
                        if (coupon.getMinOrderAmount() != null && subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                            return BigDecimal.ZERO;
                        }
                        return d;
                    })
                    .orElse(BigDecimal.ZERO);
        }

        BigDecimal shippingCost = calculateShipping(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(shippingCost);

        return CartDto.builder()
                .id(cart.getId())
                .items(itemDtos)
                .itemCount(cart.getItemCount())
                .subtotal(subtotal)
                .discount(discount)
                .shippingCost(shippingCost)
                .total(total)
                .couponCode(cart.getCouponCode())
                .build();
    }

    private CartItemDto mapItemToDto(CartItem item) {
        // ✅ FIX : fallback sur la première image disponible si aucune n'est marquée isPrimary
        List<ProductImage> images = item.getProduct().getImages();
        String productImage = images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElseGet(() -> images.isEmpty() ? null : images.get(0).getUrl());

        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSlug(item.getProduct().getSlug())
                .productImage(productImage)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .sizeName(item.getVariant() != null && item.getVariant().getSize() != null
                        ? item.getVariant().getSize().getName() : null)
                .colorName(item.getVariant() != null && item.getVariant().getColor() != null
                        ? item.getVariant().getColor().getName() : null)
                .colorHex(item.getVariant() != null && item.getVariant().getColor() != null
                        ? item.getVariant().getColor().getHexCode() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .availableStock(item.getVariant() != null
                        ? item.getVariant().getStock()
                        : item.getProduct().getTotalStock())
                .build();
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        // FIX L4: Read delivery config from DB instead of hardcoded 8 / 150 TND
        BigDecimal freeThreshold = appSettingService.getDecimal("delivery.free_threshold", BigDecimal.valueOf(150));
        BigDecimal fee = appSettingService.getDecimal("delivery.fee", BigDecimal.valueOf(8));
        return subtotal.compareTo(freeThreshold) >= 0 ? BigDecimal.ZERO : fee;
    }
}