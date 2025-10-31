package com.supplychainx.production.dto.response;

import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterialResponseDTO {

    private Long id;
    private ProductResponseDTO product;
    private RawMaterialResponseDTO rawMaterial;
    private Double quantity;
    private String unit;
    private Double lineCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
