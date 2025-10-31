package com.supplychainx.supply.dto.response;

import com.supplychainx.supply.enums.SupplyOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderResponseDTO {

    private Long id;
    private String orderNumber;
    private SupplierResponseDTO supplier;
    private LocalDate orderDate;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private SupplyOrderStatus status;
    private Double totalAmount;
    private List<SupplyOrderLineResponseDTO> orderLines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
