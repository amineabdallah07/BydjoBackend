package com.bydjo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String hexCode;
}
