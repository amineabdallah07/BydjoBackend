package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "FREE";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime assignedAt;

    @Column(length = 255)
    private String customerName;

    @Column(length = 255)
    private String productName;

    @Column(length = 30)
    private String orderNumber;

    @Column(length = 10)
    private String qrType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 10)
    private String size;
}
