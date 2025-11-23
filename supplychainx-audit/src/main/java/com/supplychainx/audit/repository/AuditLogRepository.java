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


    Page<AuditLog> findByEntityType(EntityType entityType, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    Page<AuditLog> findByAction(ActionType action, Pageable pageable);

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<AuditLog> findByPerformedByAndAction(String performedBy, ActionType action, Pageable pageable);

    List<AuditLog> findTop10ByEntityTypeOrderByTimestampDesc(EntityType entityType);

    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a GROUP BY a.performedBy")
    List<Object[]> countActionsByUser();

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action")
    List<Object[]> countActionsByType();

    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a GROUP BY a.entityType")
    List<Object[]> countActionsByEntityType();

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

    long countByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    void deleteByTimestampBefore(LocalDateTime date);
}
