package com.bydjo.service.impl;

import com.bydjo.dtos.common.ApiResponse;
import com.bydjo.dtos.qr.QrCodeDto;
import com.bydjo.dtos.qr.QrOrderItemDto;
import com.bydjo.entity.Order;
import com.bydjo.entity.OrderItem;
import com.bydjo.entity.QrCode;
import com.bydjo.entity.QrOrderItem;
import com.bydjo.repository.OrderItemRepository;
import com.bydjo.repository.QrCodeRepository;
import com.bydjo.repository.QrOrderItemRepository;
import com.bydjo.service.QrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrServiceImpl implements QrService {

    private final QrOrderItemRepository qrOrderItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final QrCodeRepository qrCodeRepository;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.backend-url:http://localhost:8080/api}")
    private String backendUrl;

    @Override
    @Transactional
    public QrOrderItemDto createQrOrderItem(Long orderItemId, String qrType, String content) {
        // Find the next available FREE pre-generated QR code
        QrCode freeCode = qrCodeRepository.findFirstByStatus("FREE").orElse(null);
        String qrCode;

        if (freeCode != null) {
            qrCode = freeCode.getCode();
            // Get order info for assignment tracking
            var opt = orderItemRepository.findById(orderItemId);
            if (opt.isPresent()) {
                var orderItem = opt.get();
                Order order = orderItem.getOrder();
                String customerName = order != null
                        ? (order.getUser() != null ? order.getUser().getFullName() : order.getGuestName())
                        : null;
                freeCode.setStatus("ASSIGNED");
                freeCode.setAssignedAt(LocalDateTime.now());
                freeCode.setCustomerName(customerName);
                freeCode.setProductName(orderItem.getProductName());
                freeCode.setOrderNumber(order != null ? order.getOrderNumber() : null);
                freeCode.setQrType(qrType.toUpperCase());
                freeCode.setContent(content);
                qrCodeRepository.save(freeCode);
                log.info("Assigned pre-generated QR code {} to orderItemId={}", qrCode, orderItemId);
            } else {
                // Fallback: assign without details
                freeCode.setStatus("ASSIGNED");
                freeCode.setAssignedAt(LocalDateTime.now());
                freeCode.setQrType(qrType.toUpperCase());
                freeCode.setContent(content);
                qrCodeRepository.save(freeCode);
                log.info("Assigned pre-generated QR code {} (no order details) to orderItemId={}", qrCode, orderItemId);
            }
        } else {
            qrCode = UUID.randomUUID().toString();
            log.warn("No free QR codes available, generated new one: {} (create more via /qr/codes/generate)", qrCode);
        }

        QrOrderItem entity = QrOrderItem.builder()
                .orderItemId(orderItemId)
                .qrType(qrType.toUpperCase())
                .content(content)
                .qrCode(qrCode)
                .build();

        entity = qrOrderItemRepository.save(entity);
        log.info("QR order item created: {} for orderItemId={}", qrCode, orderItemId);

        return mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getQrContent(String qrCode) {
        // First try QrOrderItem (order-flow data)
        QrOrderItem qrItem = qrOrderItemRepository.findByQrCode(qrCode).orElse(null);
        if (qrItem != null) {
            return ApiResponse.success(Map.of(
                    "qrType", qrItem.getQrType(),
                    "content", qrItem.getContent(),
                    "createdAt", qrItem.getCreatedAt()
            ));
        }
        // Fallback to QrCode (directly assigned)
        QrCode qrCodeEntity = qrCodeRepository.findByCode(qrCode).orElse(null);
        if (qrCodeEntity != null && "ASSIGNED".equals(qrCodeEntity.getStatus()) && qrCodeEntity.getQrType() != null) {
            return ApiResponse.success(Map.of(
                    "qrType", qrCodeEntity.getQrType(),
                    "content", qrCodeEntity.getContent(),
                    "createdAt", qrCodeEntity.getCreatedAt()
            ));
        }
        return ApiResponse.error("QR code not found");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<QrOrderItemDto>> getAllQrOrderItems() {
        List<QrOrderItem> items = qrOrderItemRepository.findAllByOrderByCreatedAtDesc();
        List<QrOrderItemDto> dtos = items.stream().map(this::mapToFullDto).collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @Override
    @Transactional
    public List<QrCodeDto> generateQrCodes(int count) {
        List<QrCode> codes = IntStream.range(0, count)
                .mapToObj(i -> QrCode.builder()
                        .code(UUID.randomUUID().toString())
                        .status("FREE")
                        .build())
                .collect(Collectors.toList());
        codes = qrCodeRepository.saveAll(codes);
        log.info("Generated {} free QR codes", count);
        return codes.stream().map(this::mapQrCodeToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QrCodeDto> getAllQrCodes() {
        return qrCodeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapQrCodeToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteQrCode(Long id) {
        QrCode code = qrCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR code not found with id: " + id));
        // Delete linked QrOrderItem if assigned
        qrOrderItemRepository.findByQrCode(code.getCode()).ifPresent(qrOrderItemRepository::delete);
        qrCodeRepository.delete(code);
        log.info("Deleted QR code {} (was {})", id, code.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Long>> getQrCodeStats() {
        long free = qrCodeRepository.countByStatus("FREE");
        long assigned = qrCodeRepository.countByStatus("ASSIGNED");
        return ApiResponse.success(Map.of("free", free, "assigned", assigned));
    }

    @Override
    @Transactional(readOnly = true)
    public String getRedirectUrl(String qrCode) {
        // First try QrOrderItem
        QrOrderItem qrItem = qrOrderItemRepository.findByQrCode(qrCode).orElse(null);
        if (qrItem != null && qrItem.getContent() != null) {
            if ("LINK".equals(qrItem.getQrType())) {
                String url = qrItem.getContent();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                return url;
            }
            if ("PHOTO".equals(qrItem.getQrType())) {
                return resolveAbsoluteUrl(qrItem.getContent());
            }
        }
        // Fallback to QrCode
        QrCode qrCodeEntity = qrCodeRepository.findByCode(qrCode).orElse(null);
        if (qrCodeEntity != null && "ASSIGNED".equals(qrCodeEntity.getStatus()) && qrCodeEntity.getContent() != null) {
            if ("LINK".equals(qrCodeEntity.getQrType())) {
                String url = qrCodeEntity.getContent();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                return url;
            }
            if ("PHOTO".equals(qrCodeEntity.getQrType())) {
                return resolveAbsoluteUrl(qrCodeEntity.getContent());
            }
        }
        // FREE or not found: frontend page handles display/error
        return frontendUrl + "/qr/" + qrCode;
    }

    private String resolveAbsoluteUrl(String content) {
        if (content.startsWith("http://") || content.startsWith("https://")) {
            return content;
        }
        if (content.startsWith("/")) {
            return backendUrl + content;
        }
        return backendUrl + "/" + content;
    }

    private QrCodeDto mapQrCodeToDto(QrCode entity) {
        return QrCodeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .assignedAt(entity.getAssignedAt())
                .customerName(entity.getCustomerName())
                .productName(entity.getProductName())
                .orderNumber(entity.getOrderNumber())
                .qrType(entity.getQrType())
                .content(entity.getContent())
                .build();
    }

    private QrOrderItemDto mapToDto(QrOrderItem item) {
        return QrOrderItemDto.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItemId())
                .qrType(item.getQrType())
                .content(item.getContent())
                .qrCode(item.getQrCode())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private QrOrderItemDto mapToFullDto(QrOrderItem item) {
        String productName = null;
        String orderNumber = null;
        String customerName = null;
        Long qrCodeId = null;

        // Look up the pre-generated QrCode to get its id
        var qrCodeOpt = qrCodeRepository.findByCode(item.getQrCode());
        if (qrCodeOpt.isPresent()) {
            qrCodeId = qrCodeOpt.get().getId();
        }

        var opt = orderItemRepository.findById(item.getOrderItemId());
        if (opt.isPresent()) {
            var orderItem = opt.get();
            productName = orderItem.getProductName();
            Order order = orderItem.getOrder();
            if (order != null) {
                orderNumber = order.getOrderNumber();
                customerName = order.getUser() != null
                        ? order.getUser().getFullName()
                        : order.getGuestName();
            }
        }

        return QrOrderItemDto.builder()
                .id(item.getId())
                .orderItemId(item.getOrderItemId())
                .qrType(item.getQrType())
                .content(item.getContent())
                .qrCode(item.getQrCode())
                .qrCodeId(qrCodeId)
                .createdAt(item.getCreatedAt())
                .productName(productName)
                .orderNumber(orderNumber)
                .customerName(customerName)
                .build();
    }
}
