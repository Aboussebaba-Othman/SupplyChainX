package com.supplychainx.audit.entity;

import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlert extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "minimum_stock")
    private Integer minimumStock;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolution_comment", columnDefinition = "TEXT")
    private String resolutionComment;

    @Builder.Default
    @Column(name = "email_sent", nullable = false)
    private boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void markAsResolved(String resolvedBy, String comment) {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.resolutionComment = comment;
    }

    public void markEmailAsSent() {
        this.emailSent = true;
        this.emailSentAt = LocalDateTime.now();
    }

    public boolean isCritical() {
        return alertType == AlertType.CRITICAL_STOCK || alertType == AlertType.OUT_OF_STOCK;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (Stock: %d/%d)",
                createdAt, alertType, entityType, entityName, currentStock, minimumStock);
    }
}
