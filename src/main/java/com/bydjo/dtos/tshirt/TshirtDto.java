package com.bydjo.dtos.tshirt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TshirtDto {
    private Long id;
    private String code;
    private Long ownerId;
    private Integer scanCount;
    private LocalDateTime createdAt;
}
