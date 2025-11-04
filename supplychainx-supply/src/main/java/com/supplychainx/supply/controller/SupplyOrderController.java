package com.supplychainx.supply.controller;

import com.supplychainx.common.dto.ApiResponse;
import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.supply.dto.request.SupplyOrderRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderResponseDTO;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import com.supplychainx.supply.service.SupplyOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/supply-orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes d'Approvisionnement", description = "API de gestion des commandes d'approvisionnement")
public class SupplyOrderController {

    private final SupplyOrderService supplyOrderService;

    // US13: Créer une nouvelle commande d'approvisionnement
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une commande", description = "Crée une nouvelle commande d'approvisionnement avec ses lignes")
    public ApiResponse<SupplyOrderResponseDTO> create(@Valid @RequestBody SupplyOrderRequestDTO requestDTO) {
        log.info("Requête de création d'une commande - Numéro: {}", requestDTO.getOrderNumber());
        SupplyOrderResponseDTO order = supplyOrderService.create(requestDTO);
        return ApiResponse.success("Commande créée avec succès", order);
    }

    // US14: Mettre à jour une commande
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une commande", description = "Met à jour une commande d'approvisionnement si elle n'est pas reçue ou annulée")
    public ApiResponse<SupplyOrderResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplyOrderRequestDTO requestDTO) {
        log.info("Requête de mise à jour de la commande ID: {}", id);
        SupplyOrderResponseDTO order = supplyOrderService.update(id, requestDTO);
        return ApiResponse.success("Commande mise à jour avec succès", order);
    }

    // US15: Supprimer une commande
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une commande", description = "Supprime une commande si elle n'est pas reçue")
    public void delete(@PathVariable Long id) {
        log.info("Requête de suppression de la commande ID: {}", id);
        supplyOrderService.delete(id);
    }

    // US16: Consulter une commande par ID
    @GetMapping("/{id}")
    @Operation(summary = "Consulter une commande par ID", description = "Récupère les détails d'une commande par son ID")
    public ApiResponse<SupplyOrderResponseDTO> findById(@PathVariable Long id) {
        log.debug("Requête de consultation de la commande ID: {}", id);
        SupplyOrderResponseDTO order = supplyOrderService.findById(id);
        return ApiResponse.success(order);
    }

    // Consulter une commande par numéro
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Consulter une commande par numéro", description = "Récupère les détails d'une commande par son numéro")
    public ApiResponse<SupplyOrderResponseDTO> findByOrderNumber(@PathVariable String orderNumber) {
        log.debug("Requête de consultation de la commande numéro: {}", orderNumber);
        SupplyOrderResponseDTO order = supplyOrderService.findByOrderNumber(orderNumber);
        return ApiResponse.success(order);
    }

    // US17: Lister toutes les commandes avec pagination
    @GetMapping
    @Operation(summary = "Lister toutes les commandes", description = "Récupère la liste de toutes les commandes avec pagination")
    public ApiResponse<PageResponse<SupplyOrderResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de liste de toutes les commandes - Page: {}", pageable.getPageNumber());
        PageResponse<SupplyOrderResponseDTO> orders = supplyOrderService.findAll(pageable);
        return ApiResponse.success(orders);
    }

    // Filtrer par statut
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrer par statut", description = "Récupère les commandes ayant un statut spécifique")
    public ApiResponse<List<SupplyOrderResponseDTO>> findByStatus(
            @PathVariable SupplyOrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de filtrage des commandes - Statut: {}", status);
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findByStatus(status, pageable);
        return ApiResponse.success(orders);
    }

    // Filtrer par fournisseur
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Commandes d'un fournisseur", description = "Récupère les commandes d'un fournisseur spécifique")
    public ApiResponse<List<SupplyOrderResponseDTO>> findBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de liste des commandes du fournisseur ID: {}", supplierId);
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findBySupplier(supplierId, pageable);
        return ApiResponse.success(orders);
    }

    // Filtrer par fournisseur et statut
    @GetMapping("/supplier/{supplierId}/status/{status}")
    @Operation(summary = "Commandes d'un fournisseur par statut", description = "Récupère les commandes d'un fournisseur avec un statut spécifique")
    public ApiResponse<List<SupplyOrderResponseDTO>> findBySupplierAndStatus(
            @PathVariable Long supplierId,
            @PathVariable SupplyOrderStatus status,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de liste des commandes du fournisseur ID: {} avec le statut: {}", supplierId, status);
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findBySupplierAndStatus(supplierId, status, pageable);
        return ApiResponse.success(orders);
    }

    // Filtrer par plage de dates
    @GetMapping("/date-range")
    @Operation(summary = "Commandes par période", description = "Récupère les commandes entre deux dates")
    public ApiResponse<List<SupplyOrderResponseDTO>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de liste des commandes entre {} et {}", startDate, endDate);
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findByDateRange(startDate, endDate, pageable);
        return ApiResponse.success(orders);
    }

    // Lister les commandes en retard
    @GetMapping("/delayed")
    @Operation(summary = "Commandes en retard", description = "Récupère les commandes EN_COURS dont la date de livraison prévue est dépassée")
    public ApiResponse<List<SupplyOrderResponseDTO>> findDelayedOrders() {
        log.debug("Requête de liste des commandes en retard");
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findDelayedOrders();
        return ApiResponse.success(orders);
    }

    // Lister les commandes récentes
    @GetMapping("/recent")
    @Operation(summary = "Commandes récentes", description = "Récupère les dernières commandes créées")
    public ApiResponse<List<SupplyOrderResponseDTO>> findRecentOrders(
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Requête de liste des commandes récentes");
        List<SupplyOrderResponseDTO> orders = supplyOrderService.findRecentOrders(pageable);
        return ApiResponse.success(orders);
    }

    // Mettre à jour le statut d'une commande
    @PatchMapping("/{id}/status")
    @Operation(summary = "Changer le statut", description = "Met à jour le statut d'une commande avec validation des transitions")
    public ApiResponse<SupplyOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam SupplyOrderStatus status) {
        log.info("Requête de changement de statut - Commande ID: {}, Nouveau statut: {}", id, status);
        SupplyOrderResponseDTO order = supplyOrderService.updateStatus(id, status);
        return ApiResponse.success("Statut mis à jour avec succès", order);
    }

    // Recevoir une commande
    @PatchMapping("/{id}/receive")
    @Operation(summary = "Recevoir une commande", description = "Marque une commande comme reçue et met à jour le stock des matières premières")
    public ApiResponse<SupplyOrderResponseDTO> receiveOrder(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate actualDeliveryDate) {
        log.info("Requête de réception de la commande ID: {} à la date: {}", id, actualDeliveryDate);
        SupplyOrderResponseDTO order = supplyOrderService.receiveOrder(id, actualDeliveryDate);
        return ApiResponse.success("Commande reçue avec succès, stock mis à jour", order);
    }

    // Annuler une commande
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une commande", description = "Annule une commande si elle n'est pas déjà reçue")
    public ApiResponse<SupplyOrderResponseDTO> cancelOrder(@PathVariable Long id) {
        log.info("Requête d'annulation de la commande ID: {}", id);
        SupplyOrderResponseDTO order = supplyOrderService.cancelOrder(id);
        return ApiResponse.success("Commande annulée avec succès", order);
    }

    // Compter les commandes actives d'un fournisseur
    @GetMapping("/supplier/{supplierId}/active-count")
    @Operation(summary = "Compter les commandes actives", description = "Compte les commandes actives (EN_ATTENTE + EN_COURS) d'un fournisseur")
    public ApiResponse<Long> countActiveOrdersBySupplier(@PathVariable Long supplierId) {
        log.debug("Requête de comptage des commandes actives du fournisseur ID: {}", supplierId);
        Long count = supplyOrderService.countActiveOrdersBySupplier(supplierId);
        return ApiResponse.success(count);
    }

    // Calculer le montant total par statut
    @GetMapping("/total-amount/status/{status}")
    @Operation(summary = "Montant total par statut", description = "Calcule le montant total des commandes pour un statut donné")
    public ApiResponse<Double> sumTotalAmountByStatus(@PathVariable SupplyOrderStatus status) {
        log.debug("Requête de calcul du montant total pour le statut: {}", status);
        Double totalAmount = supplyOrderService.sumTotalAmountByStatus(status);
        return ApiResponse.success(totalAmount);
    }

    // Vérifier si une commande peut être supprimée
    @GetMapping("/{id}/can-delete")
    @Operation(summary = "Vérifier la suppression possible", description = "Vérifie si une commande peut être supprimée (statut != RECUE)")
    public ApiResponse<Boolean> canBeDeleted(@PathVariable Long id) {
        log.debug("Requête de vérification de suppression de la commande ID: {}", id);
        boolean canDelete = supplyOrderService.canBeDeleted(id);
        return ApiResponse.success(canDelete);
    }

    // Vérifier si une commande peut être modifiée
    @GetMapping("/{id}/can-modify")
    @Operation(summary = "Vérifier la modification possible", description = "Vérifie si une commande peut être modifiée (statut != RECUE/ANNULEE)")
    public ApiResponse<Boolean> canBeModified(@PathVariable Long id) {
        log.debug("Requête de vérification de modification de la commande ID: {}", id);
        boolean canModify = supplyOrderService.canBeModified(id);
        return ApiResponse.success(canModify);
    }
}
