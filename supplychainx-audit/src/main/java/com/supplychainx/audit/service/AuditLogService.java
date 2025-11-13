package com.supplychainx.audit.service;

import com.supplychainx.audit.dto.request.AuditLogRequestDTO;
import com.supplychainx.audit.dto.response.AuditLogResponseDTO;
import com.supplychainx.audit.entity.AuditLog;
import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.mapper.AuditLogMapper;
import com.supplychainx.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service pour gérer les logs d'audit
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Créer un nouveau log d'audit
     */
    @Transactional
    public AuditLogResponseDTO createAuditLog(AuditLogRequestDTO requestDTO) {
        log.debug("Creating audit log for {} {} by {}",
                requestDTO.getAction(), requestDTO.getEntityType(), requestDTO.getPerformedBy());

        AuditLog auditLog = auditLogMapper.toEntity(requestDTO);
        AuditLog savedLog = auditLogRepository.save(auditLog);

        return auditLogMapper.toResponseDTO(savedLog);
    }

    /**
     * Méthode simplifiée pour logger une action
     */
    @Transactional
    public void logAction(EntityType entityType, Long entityId, ActionType action,
                         String performedBy, String details) {
        AuditLogRequestDTO requestDTO = AuditLogRequestDTO.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .details(details)
                .build();

        createAuditLog(requestDTO);
    }

    /**
     * Trouver tous les logs avec pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toResponseDTO);
    }

    /**
     * Trouver les logs par type d'entité
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByEntityType(EntityType entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable)
                .map(auditLogMapper::toResponseDTO);
    }

    /**
     * Trouver les logs pour une entité spécifique
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponseDTO> findByEntity(EntityType entityType, Long entityId) {
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return auditLogMapper.toResponseDTOList(logs);
    }

    /**
     * Trouver les logs par type d'action
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByAction(ActionType action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(auditLogMapper::toResponseDTO);
    }

    /**
     * Trouver les logs par utilisateur
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByUser(String username, Pageable pageable) {
        return auditLogRepository.findByPerformedBy(username, pageable)
                .map(auditLogMapper::toResponseDTO);
    }

    /**
     * Trouver les logs dans une période
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate,
                                                     Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable)
                .map(auditLogMapper::toResponseDTO);
    }

    /**
     * Recherche avancée avec filtres multiples
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDTO> searchAuditLogs(
            EntityType entityType,
            ActionType action,
            String performedBy,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        return auditLogRepository.searchAuditLogs(
                entityType, action, performedBy, startDate, endDate, pageable
        ).map(auditLogMapper::toResponseDTO);
    }

    /**
     * Obtenir les statistiques par utilisateur
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatisticsByUser() {
        List<Object[]> results = auditLogRepository.countActionsByUser();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Obtenir les statistiques par type d'action
     */
    @Transactional(readOnly = true)
    public Map<ActionType, Long> getStatisticsByActionType() {
        List<Object[]> results = auditLogRepository.countActionsByType();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (ActionType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Obtenir les statistiques par type d'entité
     */
    @Transactional(readOnly = true)
    public Map<EntityType, Long> getStatisticsByEntityType() {
        List<Object[]> results = auditLogRepository.countActionsByEntityType();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (EntityType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Compter le nombre d'actions pour une entité
     */
    @Transactional(readOnly = true)
    public long countByEntity(EntityType entityType, Long entityId) {
        return auditLogRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Nettoyage des anciens logs (plus de X jours)
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        log.info("Cleaning up audit logs older than {}", cutoffDate);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);
    }
}
