package com.supplychainx.audit.scheduler;

import com.supplychainx.audit.dto.request.StockAlertRequestDTO;
import com.supplychainx.audit.dto.response.StockAlertResponseDTO;
import com.supplychainx.audit.entity.StockAlert;
import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.repository.StockAlertRepository;
import com.supplychainx.audit.service.EmailService;
import com.supplychainx.audit.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler pour vérifier automatiquement les stocks et envoyer des alertes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockAlertScheduler {

    private final StockAlertService stockAlertService;
    private final StockAlertRepository stockAlertRepository;
    private final EmailService emailService;

    /**
     * Vérifier les stocks toutes les 6 heures
     * Cron: 0 0 star/6 * * * (à minuit, 6h, midi, 18h)
     */
    @Scheduled(cron = "${app.scheduler.stock-check.cron:0 0 6-18/6 * * *}")
    public void checkLowStockLevels() {
        log.info("Starting scheduled stock level check...");

        // TODO: Implémenter la vérification des stocks
        // Cette méthode sera complétée une fois que les modules supply et production seront disponibles
        
        /*
        // Exemple d'implémentation future:
        List<RawMaterial> lowStockMaterials = rawMaterialRepository.findLowStockMaterials();
        for (RawMaterial material : lowStockMaterials) {
            createStockAlert(material);
        }
        
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        for (Product product : lowStockProducts) {
            createStockAlert(product);
        }
        */

        log.info("Stock level check completed");
    }

    /**
     * Envoyer les emails pour les alertes non envoyées
     * Cron: 0 star/30 * * * * (toutes les 30 minutes)
     */
    @Scheduled(cron = "${app.scheduler.email-alerts.cron:0 0/30 * * * *}")
    public void sendPendingAlertEmails() {
        log.info("Starting to send pending alert emails...");

        List<StockAlertResponseDTO> pendingAlerts = stockAlertService.findAlertsWithoutEmail();

        for (StockAlertResponseDTO alertDTO : pendingAlerts) {
            try {
                // Récupérer l'entité pour l'envoyer par email
                StockAlert alert = stockAlertRepository.findById(alertDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Alert not found"));

                emailService.sendStockAlert(alert);
                stockAlertService.markEmailAsSent(alert.getId());

                log.info("Email sent for alert ID: {}", alert.getId());

            } catch (Exception e) {
                log.error("Failed to send email for alert ID: {}: {}",
                        alertDTO.getId(), e.getMessage());
            }
        }

        log.info("Pending alert emails processing completed. Sent: {} emails", pendingAlerts.size());
    }

    /**
     * Nettoyage des anciens logs et alertes (tous les dimanches à 2h du matin)
     * Cron: 0 0 2 * * SUN
     */
    @Scheduled(cron = "${app.scheduler.cleanup.cron:0 0 2 * * SUN}")
    public void cleanupOldData() {
        log.info("Starting cleanup of old data...");

        int auditLogRetentionDays = 365; // 1 an
        int alertRetentionDays = 90; // 3 mois

        try {
            // Nettoyage des alertes résolues anciennes
            stockAlertService.cleanupResolvedAlerts(alertRetentionDays);
            log.info("Cleaned up resolved alerts older than {} days", alertRetentionDays);

        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);
        }

        log.info("Cleanup completed");
    }

    /**
     * Méthode utilitaire pour créer une alerte de stock
     * (à utiliser une fois les modules supply et production disponibles)
     */
    private void createStockAlertForMaterial(Long materialId, String materialName,
                                            Integer currentStock, Integer minStock) {
        AlertType alertType = determineAlertType(currentStock, minStock);

        StockAlertRequestDTO request = StockAlertRequestDTO.builder()
                .alertType(alertType)
                .entityType(EntityType.RAW_MATERIAL)
                .entityId(materialId)
                .entityName(materialName)
                .currentStock(currentStock)
                .minimumStock(minStock)
                .message(String.format("Stock faible pour %s: %d/%d", materialName, currentStock, minStock))
                .build();

        stockAlertService.createAlert(request);
        log.info("Created alert for material: {} (Stock: {}/{})", materialName, currentStock, minStock);
    }

    /**
     * Méthode utilitaire pour créer une alerte pour un produit
     */
    private void createStockAlertForProduct(Long productId, String productName,
                                           Integer currentStock, Integer minStock) {
        AlertType alertType = determineAlertType(currentStock, minStock);

        StockAlertRequestDTO request = StockAlertRequestDTO.builder()
                .alertType(alertType)
                .entityType(EntityType.PRODUCT)
                .entityId(productId)
                .entityName(productName)
                .currentStock(currentStock)
                .minimumStock(minStock)
                .message(String.format("Stock faible pour %s: %d/%d", productName, currentStock, minStock))
                .build();

        stockAlertService.createAlert(request);
        log.info("Created alert for product: {} (Stock: {}/{})", productName, currentStock, minStock);
    }

    /**
     * Déterminer le type d'alerte en fonction du niveau de stock
     */
    private AlertType determineAlertType(Integer currentStock, Integer minStock) {
        if (currentStock == 0) {
            return AlertType.OUT_OF_STOCK;
        } else if (currentStock < minStock / 2) {
            return AlertType.CRITICAL_STOCK;
        } else {
            return AlertType.LOW_STOCK;
        }
    }
}
