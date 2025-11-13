package com.supplychainx.audit.entity;

import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une entrée d'audit dans le système
 * Permet de tracer toutes les actions effectuées sur les entités
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    /**
     * Type d'entité concernée par l'audit
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    /**
     * ID de l'entité concernée
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Type d'action effectuée
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ActionType action;

    /**
     * Utilisateur ayant effectué l'action
     */
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    /**
     * Date et heure de l'action
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Détails supplémentaires de l'action (JSON ou texte)
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Adresse IP de l'utilisateur
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * Anciennes valeurs (avant modification) - JSON
     */
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    /**
     * Nouvelles valeurs (après modification) - JSON
     */
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Méthode pour formater l'audit log en string lisible
     */
    @Override
    public String toString() {
        return String.format("[%s] %s performed %s on %s (ID: %d)",
                timestamp, performedBy, action, entityType, entityId);
    }
}
