package com.supplychainx.audit.controller;

import com.supplychainx.audit.dto.request.ResolveAlertRequestDTO;
import com.supplychainx.audit.dto.request.StockAlertRequestDTO;
import com.supplychainx.audit.dto.response.StockAlertResponseDTO;
import com.supplychainx.audit.enums.AlertType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.scheduler.StockAlertScheduler;
import com.supplychainx.audit.service.StockAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour gérer les alertes de stock
 */
@RestController
@RequestMapping("/api/audit/alerts")
@RequiredArgsConstructor
@Tag(name = "Stock Alerts", description = "Gestion des alertes de stock")
public class StockAlertController {

    private final StockAlertService stockAlertService;
    private final StockAlertScheduler stockAlertScheduler;

    @PostMapping
    @Operation(summary = "Créer une nouvelle alerte de stock")
    public ResponseEntity<StockAlertResponseDTO> createAlert(@Valid @RequestBody StockAlertRequestDTO requestDTO) {
        StockAlertResponseDTO response = stockAlertService.createAlert(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une alerte par ID")
    public ResponseEntity<StockAlertResponseDTO> getAlertById(@PathVariable Long id) {
        StockAlertResponseDTO alert = stockAlertService.findById(id);
        return ResponseEntity.ok(alert);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les alertes avec pagination")
    public ResponseEntity<Page<StockAlertResponseDTO>> getAllAlerts(Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.findAll(pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unresolved")
    @Operation(summary = "Récupérer les alertes non résolues")
    public ResponseEntity<Page<StockAlertResponseDTO>> getUnresolvedAlerts(Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.findUnresolvedAlerts(pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/resolved")
    @Operation(summary = "Récupérer les alertes résolues")
    public ResponseEntity<Page<StockAlertResponseDTO>> getResolvedAlerts(Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.findResolvedAlerts(pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/type/{alertType}")
    @Operation(summary = "Récupérer les alertes par type")
    public ResponseEntity<Page<StockAlertResponseDTO>> getAlertsByType(
            @PathVariable AlertType alertType,
            Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.findByAlertType(alertType, pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/type/{alertType}/unresolved")
    @Operation(summary = "Récupérer les alertes non résolues par type")
    public ResponseEntity<Page<StockAlertResponseDTO>> getUnresolvedAlertsByType(
            @PathVariable AlertType alertType,
            Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.findUnresolvedByAlertType(alertType, pageable);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Récupérer les alertes pour une entité spécifique")
    public ResponseEntity<List<StockAlertResponseDTO>> getAlertsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        List<StockAlertResponseDTO> alerts = stockAlertService.findByEntity(entityType, entityId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/entity/{entityType}/{entityId}/unresolved")
    @Operation(summary = "Récupérer les alertes non résolues pour une entité")
    public ResponseEntity<List<StockAlertResponseDTO>> getUnresolvedAlertsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        List<StockAlertResponseDTO> alerts = stockAlertService.findUnresolvedByEntity(entityType, entityId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/critical/unresolved")
    @Operation(summary = "Récupérer les alertes critiques non résolues")
    public ResponseEntity<List<StockAlertResponseDTO>> getCriticalUnresolvedAlerts() {
        List<StockAlertResponseDTO> alerts = stockAlertService.findCriticalUnresolvedAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/pending-email")
    @Operation(summary = "Récupérer les alertes sans email envoyé")
    public ResponseEntity<List<StockAlertResponseDTO>> getAlertsWithoutEmail() {
        List<StockAlertResponseDTO> alerts = stockAlertService.findAlertsWithoutEmail();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche avancée d'alertes avec filtres multiples")
    public ResponseEntity<Page<StockAlertResponseDTO>> searchAlerts(
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<StockAlertResponseDTO> alerts = stockAlertService.searchAlerts(
                alertType, entityType, resolved, startDate, endDate, pageable);
        return ResponseEntity.ok(alerts);
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Résoudre une alerte")
    public ResponseEntity<StockAlertResponseDTO> resolveAlert(
            @PathVariable Long id,
            @Valid @RequestBody ResolveAlertRequestDTO requestDTO) {
        StockAlertResponseDTO response = stockAlertService.resolveAlert(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/unresolved/by-type")
    @Operation(summary = "Statistiques des alertes non résolues par type")
    public ResponseEntity<Map<AlertType, Long>> getUnresolvedStatisticsByType() {
        Map<AlertType, Long> stats = stockAlertService.getUnresolvedStatisticsByType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/unresolved/by-entity-type")
    @Operation(summary = "Statistiques des alertes non résolues par type d'entité")
    public ResponseEntity<Map<EntityType, Long>> getUnresolvedStatisticsByEntityType() {
        Map<EntityType, Long> stats = stockAlertService.getUnresolvedStatisticsByEntityType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/count/unresolved")
    @Operation(summary = "Compter les alertes non résolues")
    public ResponseEntity<Long> countUnresolvedAlerts() {
        long count = stockAlertService.countUnresolvedAlerts();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/critical/unresolved")
    @Operation(summary = "Compter les alertes critiques non résolues")
    public ResponseEntity<Long> countCriticalUnresolvedAlerts() {
        long count = stockAlertService.countCriticalUnresolvedAlerts();
        return ResponseEntity.ok(count);
    }

    @PostMapping("/trigger/stock-check")
    @Operation(summary = "Déclencher manuellement la vérification des stocks (pour tests)")
    public ResponseEntity<Map<String, String>> triggerStockCheck() {
        stockAlertScheduler.checkLowStockLevels();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Stock check triggered successfully. Alerts will be created for low stock items."
        ));
    }

    @PostMapping("/trigger/send-emails")
    @Operation(summary = "Déclencher manuellement l'envoi des emails (pour tests)")
    public ResponseEntity<Map<String, String>> triggerSendEmails() {
        stockAlertScheduler.sendPendingAlertEmails();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Email sending triggered successfully. Check logs for details."
        ));
    }
}
