package com.supplychainx.audit.entity;

import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant une alerte de stock dans le système
 * Permet de notifier les utilisateurs des problèmes de stock
 */
@Entity
@Table(name = "stock_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlert extends BaseEntity {

    /**
     * Type d'alerte
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    /**
     * Type d'entité concernée par l'alerte
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    /**
     * ID de l'entité concernée (RawMaterial ou Product)
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Nom de l'entité pour faciliter l'affichage
     */
    @Column(name = "entity_name", length = 255)
    private String entityName;

    /**
     * Message d'alerte détaillé
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Niveau de stock actuel
     */
    @Column(name = "current_stock")
    private Integer currentStock;

    /**
     * Niveau de stock minimum (seuil)
     */
    @Column(name = "minimum_stock")
    private Integer minimumStock;

    /**
     * Date de création de l'alerte
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * L'alerte a-t-elle été résolue ?
     */
    @Builder.Default
    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    /**
     * Date de résolution de l'alerte
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Utilisateur ayant résolu l'alerte
     */
    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    /**
     * Commentaire de résolution
     */
    @Column(name = "resolution_comment", columnDefinition = "TEXT")
    private String resolutionComment;

    /**
     * Email envoyé ?
     */
    @Builder.Default
    @Column(name = "email_sent", nullable = false)
    private boolean emailSent = false;

    /**
     * Date d'envoi de l'email
     */
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Marquer l'alerte comme résolue
     */
    public void markAsResolved(String resolvedBy, String comment) {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionComment = comment;
    }

    /**
     * Marquer l'email comme envoyé
     */
    public void markEmailAsSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }

    /**
     * Vérifier si l'alerte est critique
     */
    public boolean isCritical() {
        return alertType == AlertType.CRITICAL_STOCK || alertType == AlertType.OUT_OF_STOCK;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (Stock: %d/%d)",
                createdAt, alertType, entityType, entityName, currentStock, minimumStock);
    }
}
