package com.supplychainx.audit.dto.request;

import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer une alerte de stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertRequestDTO {

    @NotNull(message = "Alert type is required")
    private AlertType alertType;

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    private String entityName;

    @NotBlank(message = "Message is required")
    private String message;

    private Integer currentStock;

    private Integer minimumStock;
}
