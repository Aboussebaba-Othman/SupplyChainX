package com.supplychainx.audit.controller;

import com.supplychainx.audit.dto.request.AuditLogRequestDTO;
import com.supplychainx.audit.dto.response.AuditLogResponseDTO;
import com.supplychainx.audit.enums.ActionType;
import com.supplychainx.audit.enums.EntityType;
import com.supplychainx.audit.service.AuditLogService;
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
 * Controller REST pour gérer les logs d'audit
 */
@RestController
@RequestMapping("/api/audit/logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Gestion des logs d'audit du système")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Créer un nouveau log d'audit")
    public ResponseEntity<AuditLogResponseDTO> createAuditLog(@Valid @RequestBody AuditLogRequestDTO requestDTO) {
        AuditLogResponseDTO response = auditLogService.createAuditLog(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les logs avec pagination")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAllLogs(Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.findAll(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Récupérer les logs par type d'entité")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByEntityType(
            @PathVariable EntityType entityType,
            Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.findByEntityType(entityType, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Récupérer les logs pour une entité spécifique")
    public ResponseEntity<List<AuditLogResponseDTO>> getLogsByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        List<AuditLogResponseDTO> logs = auditLogService.findByEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Récupérer les logs par type d'action")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByAction(
            @PathVariable ActionType action,
            Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.findByAction(action, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Récupérer les logs par utilisateur")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByUser(
            @PathVariable String username,
            Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.findByUser(username, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Récupérer les logs dans une période")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche avancée de logs avec filtres multiples")
    public ResponseEntity<Page<AuditLogResponseDTO>> searchLogs(
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) ActionType action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLogResponseDTO> logs = auditLogService.searchAuditLogs(
                entityType, action, performedBy, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/statistics/by-user")
    @Operation(summary = "Statistiques d'actions par utilisateur")
    public ResponseEntity<Map<String, Long>> getStatisticsByUser() {
        Map<String, Long> stats = auditLogService.getStatisticsByUser();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/by-action-type")
    @Operation(summary = "Statistiques d'actions par type")
    public ResponseEntity<Map<ActionType, Long>> getStatisticsByActionType() {
        Map<ActionType, Long> stats = auditLogService.getStatisticsByActionType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/by-entity-type")
    @Operation(summary = "Statistiques d'actions par type d'entité")
    public ResponseEntity<Map<EntityType, Long>> getStatisticsByEntityType() {
        Map<EntityType, Long> stats = auditLogService.getStatisticsByEntityType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/entity/{entityType}/{entityId}/count")
    @Operation(summary = "Compter les actions pour une entité")
    public ResponseEntity<Long> countByEntity(
            @PathVariable EntityType entityType,
            @PathVariable Long entityId) {
        long count = auditLogService.countByEntity(entityType, entityId);
        return ResponseEntity.ok(count);
    }
}
