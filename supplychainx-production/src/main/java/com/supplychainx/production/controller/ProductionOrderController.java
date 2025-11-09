package com.supplychainx.production.controller;

import com.supplychainx.production.dto.request.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.response.ProductionOrderResponseDTO;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.service.ProductionOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/production/production-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Production Orders", description = "API de gestion des ordres de production")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    @PostMapping
    @Operation(summary = "Créer un nouvel ordre de production", description = "Crée un nouvel ordre de production avec statut PLANIFIE")
    @PreAuthorize("@securityExpressions.hasAnyPermission('PRODUCTION_ORDER_CREATE')")
    public ResponseEntity<ProductionOrderResponseDTO> createProductionOrder(@Valid @RequestBody ProductionOrderRequestDTO requestDTO) {
        log.info("Requête de création d'ordre de production: {}", requestDTO.getOrderNumber());
        ProductionOrderResponseDTO response = productionOrderService.createProductionOrder(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un ordre de production par ID", description = "Récupère les détails d'un ordre de production par son identifiant")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<ProductionOrderResponseDTO> getProductionOrderById(@PathVariable Long id) {
        log.info("Requête de récupération de l'ordre de production ID: {}", id);
        ProductionOrderResponseDTO response = productionOrderService.getProductionOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Récupérer un ordre de production par numéro", description = "Récupère les détails d'un ordre de production par son numéro unique")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<ProductionOrderResponseDTO> getProductionOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Requête de récupération de l'ordre de production avec le numéro: {}", orderNumber);
        ProductionOrderResponseDTO response = productionOrderService.getProductionOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les ordres de production", description = "Récupère la liste paginée de tous les ordres de production")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<Page<ProductionOrderResponseDTO>> getAllProductionOrders(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("Requête de récupération de tous les ordres de production - Page: {}, Size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductionOrderResponseDTO> response = productionOrderService.getAllProductionOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les ordres de production par statut", description = "Récupère tous les ordres de production d'un statut spécifique")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<Page<ProductionOrderResponseDTO>> getProductionOrdersByStatus(
            @PathVariable ProductionOrderStatus status,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("Requête de récupération des ordres de production avec le statut: {}", status);
        Page<ProductionOrderResponseDTO> response = productionOrderService.getProductionOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Récupérer les ordres de production par produit", description = "Récupère tous les ordres de production pour un produit spécifique")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<Page<ProductionOrderResponseDTO>> getProductionOrdersByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("Requête de récupération des ordres de production pour le produit ID: {}", productId);
        Page<ProductionOrderResponseDTO> response = productionOrderService.getProductionOrdersByProduct(productId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/delayed")
    @Operation(summary = "Récupérer les ordres de production retardés", description = "Récupère tous les ordres de production en cours et en retard")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<List<ProductionOrderResponseDTO>> getDelayedProductionOrders() {
        log.info("Requête de récupération des ordres de production retardés");
        List<ProductionOrderResponseDTO> response = productionOrderService.getDelayedProductionOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Récupérer les ordres de production par période", description = "Récupère les ordres de production planifiés entre deux dates")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_READ')")
    public ResponseEntity<Page<ProductionOrderResponseDTO>> getProductionOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        log.info("Requête de récupération des ordres de production entre {} et {}", startDate, endDate);
        Page<ProductionOrderResponseDTO> response = productionOrderService.getProductionOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un ordre de production", description = "Met à jour les informations d'un ordre de production (sauf s'il est terminé ou annulé)")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_UPDATE')")
    public ResponseEntity<ProductionOrderResponseDTO> updateProductionOrder(
            @PathVariable Long id,
            @Valid @RequestBody ProductionOrderRequestDTO requestDTO) {
        log.info("Requête de mise à jour de l'ordre de production ID: {}", id);
        ProductionOrderResponseDTO response = productionOrderService.updateProductionOrder(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Démarrer la production", description = "Démarre la production d'un ordre (vérifie la disponibilité des matières premières)")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_START')")
    public ResponseEntity<ProductionOrderResponseDTO> startProduction(@PathVariable Long id) {
        log.info("Requête de démarrage de la production pour l'ordre ID: {}", id);
        ProductionOrderResponseDTO response = productionOrderService.startProduction(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Terminer la production", description = "Termine la production (consomme les matières premières et ajoute les produits finis au stock)")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_COMPLETE')")
    public ResponseEntity<ProductionOrderResponseDTO> completeProduction(@PathVariable Long id) {
        log.info("Requête de finalisation de la production pour l'ordre ID: {}", id);
        ProductionOrderResponseDTO response = productionOrderService.completeProduction(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler un ordre de production", description = "Annule un ordre de production (sauf s'il est déjà terminé)")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_CANCEL')")
    public ResponseEntity<ProductionOrderResponseDTO> cancelProductionOrder(@PathVariable Long id) {
        log.info("Requête d'annulation de l'ordre de production ID: {}", id);
        ProductionOrderResponseDTO response = productionOrderService.cancelProductionOrder(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un ordre de production", description = "Supprime un ordre de production (uniquement si planifié ou annulé)")
    @PreAuthorize("@securityExpressions.hasPermission('PRODUCTION_ORDER_DELETE')")
    public ResponseEntity<Void> deleteProductionOrder(@PathVariable Long id) {
        log.info("Requête de suppression de l'ordre de production ID: {}", id);
        productionOrderService.deleteProductionOrder(id);
        return ResponseEntity.noContent().build();
    }
}
