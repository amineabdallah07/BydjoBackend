package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String guestName;

    @Column(nullable = false)
    @Builder.Default
    private Integer rating = 5;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    @Builder.Default
    private Boolean approved = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
