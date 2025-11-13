package com.supplychainx.audit.dto.response;

import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour une alerte de stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertResponseDTO {

    private Long id;
    private AlertType alertType;
    private EntityType entityType;
    private Long entityId;
    private String entityName;
    private String message;
    private Integer currentStock;
    private Integer minimumStock;
    private LocalDateTime createdAt;
    private boolean resolved;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String resolutionComment;
    private boolean emailSent;
    private LocalDateTime emailSentAt;
    private boolean critical;
}
