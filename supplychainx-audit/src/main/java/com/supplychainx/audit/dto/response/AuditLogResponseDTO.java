package com.supplychainx.audit.dto.response;

import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un log d'audit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {

    private Long id;
    private EntityType entityType;
    private Long entityId;
    private ActionType action;
    private String performedBy;
    private LocalDateTime timestamp;
    private String details;
    private String ipAddress;
    private String oldValues;
    private String newValues;
    private LocalDateTime createdAt;
}
