package com.supplychainx.audit.repository;

import com.supplychainx.audit.entity.StockAlert;
import com.supplychainx.audit.enums.AlertType;
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
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    /**
     * Trouver toutes les alertes non résolues
     */
    Page<StockAlert> findByResolvedFalse(Pageable pageable);

    /**
     * Trouver toutes les alertes résolues
     */
    Page<StockAlert> findByResolvedTrue(Pageable pageable);

    /**
     * Trouver les alertes par type
     */
    Page<StockAlert> findByAlertType(AlertType alertType, Pageable pageable);

    /**
     * Trouver les alertes non résolues par type
     */
    Page<StockAlert> findByAlertTypeAndResolvedFalse(AlertType alertType, Pageable pageable);

    /**
     * Trouver les alertes pour une entité spécifique
     */
    List<StockAlert> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Trouver les alertes non résolues pour une entité spécifique
     */
    List<StockAlert> findByEntityTypeAndEntityIdAndResolvedFalse(EntityType entityType, Long entityId);

    /**
     * Trouver les alertes critiques non résolues
     */
    @Query("SELECT s FROM StockAlert s WHERE s.resolved = false AND " +
           "(s.alertType = 'CRITICAL_STOCK' OR s.alertType = 'OUT_OF_STOCK')")
    List<StockAlert> findCriticalUnresolvedAlerts();

    /**
     * Trouver les alertes créées dans une période donnée
     */
    Page<StockAlert> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Trouver les alertes pour lesquelles l'email n'a pas été envoyé
     */
    List<StockAlert> findByEmailSentFalseAndResolvedFalse();

    /**
     * Vérifier si une alerte existe déjà pour une entité
     */
    boolean existsByEntityTypeAndEntityIdAndResolvedFalse(EntityType entityType, Long entityId);

    /**
     * Compter les alertes non résolues par type
     */
    @Query("SELECT s.alertType, COUNT(s) FROM StockAlert s WHERE s.resolved = false GROUP BY s.alertType")
    List<Object[]> countUnresolvedAlertsByType();

    /**
     * Compter les alertes non résolues par type d'entité
     */
    @Query("SELECT s.entityType, COUNT(s) FROM StockAlert s WHERE s.resolved = false GROUP BY s.entityType")
    List<Object[]> countUnresolvedAlertsByEntityType();

    /**
     * Recherche avancée avec filtres multiples
     */
    @Query("SELECT s FROM StockAlert s WHERE " +
           "(:alertType IS NULL OR s.alertType = :alertType) AND " +
           "(:entityType IS NULL OR s.entityType = :entityType) AND " +
           "(:resolved IS NULL OR s.resolved = :resolved) AND " +
           "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<StockAlert> searchAlerts(
            @Param("alertType") AlertType alertType,
            @Param("entityType") EntityType entityType,
            @Param("resolved") Boolean resolved,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Trouver les alertes les plus anciennes non résolues
     */
    List<StockAlert> findTop10ByResolvedFalseOrderByCreatedAtAsc();

    /**
     * Supprimer les alertes résolues plus anciennes qu'une date (nettoyage)
     */
    void deleteByResolvedTrueAndResolvedAtBefore(LocalDateTime date);

    /**
     * Compter le nombre total d'alertes non résolues
     */
    long countByResolvedFalse();

    /**
     * Compter le nombre d'alertes critiques non résolues
     */
    @Query("SELECT COUNT(s) FROM StockAlert s WHERE s.resolved = false AND " +
           "(s.alertType = 'CRITICAL_STOCK' OR s.alertType = 'OUT_OF_STOCK')")
    long countCriticalUnresolvedAlerts();
}
