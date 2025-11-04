package com.supplychainx.supply.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderLineResponseDTO {

    private Long id;
    private RawMaterialResponseDTO material;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
