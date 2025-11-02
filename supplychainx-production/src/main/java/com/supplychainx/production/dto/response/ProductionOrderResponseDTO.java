package com.supplychainx.production.dto.response;

import com.supplychainx.production.enums.ProductionOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderResponseDTO {

    private Long id;
    private String orderNumber;
    private ProductResponseDTO product;
    private Integer quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProductionOrderStatus status;
    private Integer estimatedTime;
    private String priority;
    private boolean delayed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
