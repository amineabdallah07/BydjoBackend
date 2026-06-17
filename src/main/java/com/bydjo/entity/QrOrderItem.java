package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "qr_type", nullable = false, length = 10)
    private String qrType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "qr_code", nullable = false, unique = true, length = 36)
    private String qrCode;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
