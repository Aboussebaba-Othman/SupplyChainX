package com.supplychainx.audit.repository;

import com.supplychainx.audit.entity.AuditLog;
import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Trouver tous les logs par type d'entité
     */
    Page<AuditLog> findByEntityType(EntityType entityType, Pageable pageable);

    /**
     * Trouver tous les logs pour une entité spécifique
     */
    List<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Trouver tous les logs par type d'action
     */
    Page<AuditLog> findByAction(ActionType action, Pageable pageable);

    /**
     * Trouver tous les logs effectués par un utilisateur
     */
    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    /**
     * Trouver tous les logs dans une période donnée
     */
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Trouver les logs par utilisateur et type d'action
     */
    Page<AuditLog> findByPerformedByAndAction(String performedBy, ActionType action, Pageable pageable);

    /**
     * Trouver les derniers logs pour un type d'entité
     */
    List<AuditLog> findTop10ByEntityTypeOrderByTimestampDesc(EntityType entityType);

    /**
     * Statistiques d'actions par utilisateur
     */
    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a GROUP BY a.performedBy")
    List<Object[]> countActionsByUser();

    /**
     * Statistiques d'actions par type
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action")
    List<Object[]> countActionsByType();

    /**
     * Statistiques d'actions par entité
     */
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a GROUP BY a.entityType")
    List<Object[]> countActionsByEntityType();

    /**
     * Recherche avancée avec filtres multiples
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:performedBy IS NULL OR a.performedBy LIKE %:performedBy%) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("entityType") EntityType entityType,
            @Param("action") ActionType action,
            @Param("performedBy") String performedBy,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Compter le nombre total d'actions pour une entité
     */
    long countByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Supprimer les logs plus anciens qu'une date donnée (pour nettoyage)
     */
    void deleteByTimestampBefore(LocalDateTime date);
}
