package com.supplychainx.audit.dto.request;

import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer un log d'audit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequestDTO {

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    @NotNull(message = "Action type is required")
    private ActionType action;

    @NotNull(message = "Performed by is required")
    private String performedBy;

    private String details;

    private String ipAddress;

    private String oldValues;

    private String newValues;
}
