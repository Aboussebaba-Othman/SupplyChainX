package com.supplychainx.production.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String category;
    private Integer stock;
    private Integer stockMin;
    private String unit;
    private Double unitPrice;
    private Double productionCost;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
