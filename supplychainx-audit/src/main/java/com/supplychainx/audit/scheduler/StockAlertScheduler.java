package com.supplychainx.audit.scheduler;

import com.supplychainx.audit.dto.request.StockAlertRequestDTO;
import com.supplychainx.audit.dto.response.StockAlertResponseDTO;
import com.supplychainx.audit.entity.StockAlert;
import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.repository.StockAlertRepository;
import com.supplychainx.audit.service.EmailService;
import com.supplychainx.audit.service.StockAlertService;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.repository.ProductRepository;
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
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductRepository productRepository;

    /**
     * Vérifier les stocks toutes les 2 heures
     * Cron: 0 0 *\/2 * * * (toutes les 2 heures: 0h, 2h, 4h, 6h, 8h, 10h, 12h, 14h, 16h, 18h, 20h, 22h)
     */
    @Scheduled(cron = "${app.scheduler.stock-check.cron:0 0 6-18/6 * * *}")
    public void checkLowStockLevels() {
        log.info("Starting scheduled stock level check...");

        int alertsCreated = 0;

        try {
            // Vérifier les matières premières avec stock faible
            List<RawMaterial> lowStockMaterials = rawMaterialRepository.findLowStockMaterials();
            log.info("Found {} raw materials with low stock", lowStockMaterials.size());

            for (RawMaterial material : lowStockMaterials) {
                // Vérifier si une alerte non résolue existe déjà pour ce matériau
                boolean alertExists = stockAlertRepository.existsByEntityTypeAndEntityIdAndResolvedFalse(
                        EntityType.RAW_MATERIAL, 
                        material.getId()
                );

                if (!alertExists) {
                    createStockAlertForMaterial(
                            material.getId(),
                            material.getName(),
                            material.getStock(),
                            material.getStockMin()
                    );
                    alertsCreated++;
                } else {
                    log.debug("Alert already exists for material: {}", material.getName());
                }
            }

            // Vérifier les produits avec stock faible
            List<Product> lowStockProducts = productRepository.findLowStockProducts();
            log.info("Found {} products with low stock", lowStockProducts.size());

            for (Product product : lowStockProducts) {
                // Vérifier si une alerte non résolue existe déjà pour ce produit
                boolean alertExists = stockAlertRepository.existsByEntityTypeAndEntityIdAndResolvedFalse(
                        EntityType.PRODUCT, 
                        product.getId()
                );

                if (!alertExists) {
                    createStockAlertForProduct(
                            product.getId(),
                            product.getName(),
                            product.getStock().intValue(),
                            product.getStockMin().intValue()
                    );
                    alertsCreated++;
                } else {
                    log.debug("Alert already exists for product: {}", product.getName());
                }
            }

            log.info("Stock level check completed. Created {} new alerts", alertsCreated);

        } catch (Exception e) {
            log.error("Error during stock level check: {}", e.getMessage(), e);
        }
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
