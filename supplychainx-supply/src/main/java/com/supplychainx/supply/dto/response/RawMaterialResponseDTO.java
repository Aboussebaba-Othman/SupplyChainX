package com.supplychainx.supply.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawMaterialResponseDTO {

    private Long id;
    private String code;
    private String name;
    private Integer stock;
    private Integer stockMin;
    private String unit;
    private Double unitPrice;
    private String category;
    private boolean lowStock;
    private List<SupplierResponseDTO> suppliers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
