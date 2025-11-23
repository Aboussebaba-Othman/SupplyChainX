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

    Page<StockAlert> findByResolvedFalse(Pageable pageable);

    Page<StockAlert> findByResolvedTrue(Pageable pageable);

    Page<StockAlert> findByAlertType(AlertType alertType, Pageable pageable);

    Page<StockAlert> findByAlertTypeAndResolvedFalse(AlertType alertType, Pageable pageable);

    List<StockAlert> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    List<StockAlert> findByEntityTypeAndEntityIdAndResolvedFalse(EntityType entityType, Long entityId);

    @Query("SELECT s FROM StockAlert s WHERE s.resolved = false AND " +
           "(s.alertType = 'CRITICAL_STOCK' OR s.alertType = 'OUT_OF_STOCK')")
    List<StockAlert> findCriticalUnresolvedAlerts();

    Page<StockAlert> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<StockAlert> findByEmailSentFalseAndResolvedFalse();

    boolean existsByEntityTypeAndEntityIdAndResolvedFalse(EntityType entityType, Long entityId);

    @Query("SELECT s.alertType, COUNT(s) FROM StockAlert s WHERE s.resolved = false GROUP BY s.alertType")
    List<Object[]> countUnresolvedAlertsByType();

    @Query("SELECT s.entityType, COUNT(s) FROM StockAlert s WHERE s.resolved = false GROUP BY s.entityType")
    List<Object[]> countUnresolvedAlertsByEntityType();

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

    List<StockAlert> findTop10ByResolvedFalseOrderByCreatedAtAsc();

    void deleteByResolvedTrueAndResolvedAtBefore(LocalDateTime date);

    long countByResolvedFalse();
    @Query("SELECT COUNT(s) FROM StockAlert s WHERE s.resolved = false AND " +
           "(s.alertType = 'CRITICAL_STOCK' OR s.alertType = 'OUT_OF_STOCK')")
    long countCriticalUnresolvedAlerts();
}
