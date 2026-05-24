package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "size_id", "color_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(length = 50)
    private String sku;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
