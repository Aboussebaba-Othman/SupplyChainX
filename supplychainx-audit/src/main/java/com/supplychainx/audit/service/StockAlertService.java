package com.supplychainx.audit.service;

import com.supplychainx.audit.dto.request.ResolveAlertRequestDTO;
import com.supplychainx.audit.dto.request.StockAlertRequestDTO;
import com.supplychainx.audit.dto.response.StockAlertResponseDTO;
import com.supplychainx.audit.entity.StockAlert;
import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.mapper.StockAlertMapper;
import com.supplychainx.audit.repository.StockAlertRepository;
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
 * Service pour gérer les alertes de stock
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final StockAlertMapper stockAlertMapper;

    /**
     * Créer une nouvelle alerte de stock
     */
    @Transactional
    public StockAlertResponseDTO createAlert(StockAlertRequestDTO requestDTO) {
        log.debug("Creating stock alert for {} {}: {}",
                requestDTO.getEntityType(), requestDTO.getEntityId(), requestDTO.getAlertType());

        // Vérifier si une alerte non résolue existe déjà
        boolean exists = stockAlertRepository.existsByEntityTypeAndEntityIdAndResolvedFalse(
                requestDTO.getEntityType(), requestDTO.getEntityId()
        );

        if (exists) {
            log.warn("An unresolved alert already exists for {} {}",
                    requestDTO.getEntityType(), requestDTO.getEntityId());
        }

        StockAlert alert = stockAlertMapper.toEntity(requestDTO);
        StockAlert savedAlert = stockAlertRepository.save(alert);

        return stockAlertMapper.toResponseDTO(savedAlert);
    }

    /**
     * Trouver une alerte par ID
     */
    @Transactional(readOnly = true)
    public StockAlertResponseDTO findById(Long id) {
        StockAlert alert = stockAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
        return stockAlertMapper.toResponseDTO(alert);
    }

    /**
     * Trouver toutes les alertes avec pagination
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> findAll(Pageable pageable) {
        return stockAlertRepository.findAll(pageable)
                .map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Trouver les alertes non résolues
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> findUnresolvedAlerts(Pageable pageable) {
        return stockAlertRepository.findByResolvedFalse(pageable)
                .map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Trouver les alertes résolues
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> findResolvedAlerts(Pageable pageable) {
        return stockAlertRepository.findByResolvedTrue(pageable)
                .map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Trouver les alertes par type
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> findByAlertType(AlertType alertType, Pageable pageable) {
        return stockAlertRepository.findByAlertType(alertType, pageable)
                .map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Trouver les alertes non résolues par type
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> findUnresolvedByAlertType(AlertType alertType, Pageable pageable) {
        return stockAlertRepository.findByAlertTypeAndResolvedFalse(alertType, pageable)
                .map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Trouver les alertes pour une entité spécifique
     */
    @Transactional(readOnly = true)
    public List<StockAlertResponseDTO> findByEntity(EntityType entityType, Long entityId) {
        List<StockAlert> alerts = stockAlertRepository.findByEntityTypeAndEntityId(entityType, entityId);
        return stockAlertMapper.toResponseDTOList(alerts);
    }

    /**
     * Trouver les alertes non résolues pour une entité
     */
    @Transactional(readOnly = true)
    public List<StockAlertResponseDTO> findUnresolvedByEntity(EntityType entityType, Long entityId) {
        List<StockAlert> alerts = stockAlertRepository
                .findByEntityTypeAndEntityIdAndResolvedFalse(entityType, entityId);
        return stockAlertMapper.toResponseDTOList(alerts);
    }

    /**
     * Trouver les alertes critiques non résolues
     */
    @Transactional(readOnly = true)
    public List<StockAlertResponseDTO> findCriticalUnresolvedAlerts() {
        List<StockAlert> alerts = stockAlertRepository.findCriticalUnresolvedAlerts();
        return stockAlertMapper.toResponseDTOList(alerts);
    }

    /**
     * Trouver les alertes sans email envoyé
     */
    @Transactional(readOnly = true)
    public List<StockAlertResponseDTO> findAlertsWithoutEmail() {
        List<StockAlert> alerts = stockAlertRepository.findByEmailSentFalseAndResolvedFalse();
        return stockAlertMapper.toResponseDTOList(alerts);
    }

    /**
     * Recherche avancée avec filtres multiples
     */
    @Transactional(readOnly = true)
    public Page<StockAlertResponseDTO> searchAlerts(
            AlertType alertType,
            EntityType entityType,
            Boolean resolved,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        return stockAlertRepository.searchAlerts(
                alertType, entityType, resolved, startDate, endDate, pageable
        ).map(stockAlertMapper::toResponseDTO);
    }

    /**
     * Résoudre une alerte
     */
    @Transactional
    public StockAlertResponseDTO resolveAlert(Long alertId, ResolveAlertRequestDTO requestDTO) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));

        if (alert.isResolved()) {
            throw new RuntimeException("Alert is already resolved");
        }

        alert.markAsResolved(requestDTO.getResolvedBy(), requestDTO.getResolutionComment());
        StockAlert updatedAlert = stockAlertRepository.save(alert);

        log.info("Alert {} resolved by {}", alertId, requestDTO.getResolvedBy());

        return stockAlertMapper.toResponseDTO(updatedAlert);
    }

    /**
     * Marquer l'email comme envoyé
     */
    @Transactional
    public void markEmailAsSent(Long alertId) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + alertId));

        alert.markEmailAsSent();
        stockAlertRepository.save(alert);

        log.debug("Email marked as sent for alert {}", alertId);
    }

    /**
     * Statistiques des alertes non résolues par type
     */
    @Transactional(readOnly = true)
    public Map<AlertType, Long> getUnresolvedStatisticsByType() {
        List<Object[]> results = stockAlertRepository.countUnresolvedAlertsByType();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (AlertType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Statistiques des alertes non résolues par type d'entité
     */
    @Transactional(readOnly = true)
    public Map<EntityType, Long> getUnresolvedStatisticsByEntityType() {
        List<Object[]> results = stockAlertRepository.countUnresolvedAlertsByEntityType();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (EntityType) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Compter les alertes non résolues
     */
    @Transactional(readOnly = true)
    public long countUnresolvedAlerts() {
        return stockAlertRepository.countByResolvedFalse();
    }

    /**
     * Compter les alertes critiques non résolues
     */
    @Transactional(readOnly = true)
    public long countCriticalUnresolvedAlerts() {
        return stockAlertRepository.countCriticalUnresolvedAlerts();
    }

    /**
     * Nettoyage des anciennes alertes résolues
     */
    @Transactional
    public void cleanupResolvedAlerts(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        log.info("Cleaning up resolved alerts older than {}", cutoffDate);
        stockAlertRepository.deleteByResolvedTrueAndResolvedAtBefore(cutoffDate);
    }
}
