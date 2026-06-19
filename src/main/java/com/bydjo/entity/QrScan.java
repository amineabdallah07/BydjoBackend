package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "qr_scans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_code", nullable = false, length = 36)
    private String qrCode;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "scanned_at", updatable = false)
    private LocalDateTime scannedAt;
}
