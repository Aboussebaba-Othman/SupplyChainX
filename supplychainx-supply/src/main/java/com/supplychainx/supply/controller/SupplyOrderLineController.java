package com.supplychainx.supply.controller;

import com.supplychainx.common.dto.ApiResponse;
import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderLineResponseDTO;
import com.supplychainx.supply.service.SupplyOrderLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supply-order-lines")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lignes de Commande", description = "API de gestion des lignes de commande d'approvisionnement")
public class SupplyOrderLineController {

    private final SupplyOrderLineService supplyOrderLineService;

    // Créer une nouvelle ligne de commande
    @PostMapping("/order/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une ligne à une commande", description = "Ajoute une nouvelle ligne à une commande d'approvisionnement existante")
    public ApiResponse<SupplyOrderLineResponseDTO> create(
            @PathVariable Long orderId,
            @Valid @RequestBody SupplyOrderLineRequestDTO requestDTO) {
        log.info("Requête d'ajout d'une ligne à la commande ID: {}", orderId);
        SupplyOrderLineResponseDTO line = supplyOrderLineService.create(orderId, requestDTO);
        return ApiResponse.success("Ligne de commande ajoutée avec succès", line);
    }

    // Mettre à jour une ligne de commande
    @PutMapping("/{lineId}")
    @Operation(summary = "Mettre à jour une ligne", description = "Met à jour une ligne de commande existante")
    public ApiResponse<SupplyOrderLineResponseDTO> update(
            @PathVariable Long lineId,
            @Valid @RequestBody SupplyOrderLineRequestDTO requestDTO) {
        log.info("Requête de mise à jour de la ligne ID: {}", lineId);
        SupplyOrderLineResponseDTO line = supplyOrderLineService.update(lineId, requestDTO);
        return ApiResponse.success("Ligne de commande mise à jour avec succès", line);
    }

    // Supprimer une ligne de commande
    @DeleteMapping("/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une ligne", description = "Supprime une ligne de commande si ce n'est pas la dernière")
    public void delete(@PathVariable Long lineId) {
        log.info("Requête de suppression de la ligne ID: {}", lineId);
        supplyOrderLineService.delete(lineId);
    }

    // Consulter une ligne par ID
    @GetMapping("/{lineId}")
    @Operation(summary = "Consulter une ligne par ID", description = "Récupère les détails d'une ligne de commande par son ID")
    public ApiResponse<SupplyOrderLineResponseDTO> findById(@PathVariable Long lineId) {
        log.debug("Requête de consultation de la ligne ID: {}", lineId);
        SupplyOrderLineResponseDTO line = supplyOrderLineService.findById(lineId);
        return ApiResponse.success(line);
    }

    // Lister toutes les lignes d'une commande
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Lignes d'une commande", description = "Récupère toutes les lignes d'une commande spécifique")
    public ApiResponse<List<SupplyOrderLineResponseDTO>> findBySupplyOrder(@PathVariable Long orderId) {
        log.debug("Requête de liste des lignes de la commande ID: {}", orderId);
        List<SupplyOrderLineResponseDTO> lines = supplyOrderLineService.findBySupplyOrder(orderId);
        return ApiResponse.success(lines);
    }

    // Lister toutes les lignes contenant une matière première
    @GetMapping("/material/{materialId}")
    @Operation(summary = "Lignes d'une matière première", description = "Récupère toutes les lignes de commande contenant une matière première spécifique")
    public ApiResponse<List<SupplyOrderLineResponseDTO>> findByMaterial(@PathVariable Long materialId) {
        log.debug("Requête de liste des lignes pour la matière ID: {}", materialId);
        List<SupplyOrderLineResponseDTO> lines = supplyOrderLineService.findByMaterial(materialId);
        return ApiResponse.success(lines);
    }

    // Récupérer une ligne spécifique d'une commande pour une matière
    @GetMapping("/order/{orderId}/material/{materialId}")
    @Operation(summary = "Ligne spécifique", description = "Récupère la ligne d'une commande pour une matière première spécifique")
    public ApiResponse<SupplyOrderLineResponseDTO> findBySupplyOrderAndMaterial(
            @PathVariable Long orderId,
            @PathVariable Long materialId) {
        log.debug("Requête de la ligne pour la commande ID: {} et matière ID: {}", orderId, materialId);
        SupplyOrderLineResponseDTO line = supplyOrderLineService.findBySupplyOrderAndMaterial(orderId, materialId);
        return ApiResponse.success(line);
    }

    // Calculer la quantité totale commandée pour une matière
    @GetMapping("/material/{materialId}/total-quantity")
    @Operation(summary = "Quantité totale commandée", description = "Calcule la quantité totale commandée pour une matière première dans les commandes actives")
    public ApiResponse<Integer> sumQuantityByMaterialInActiveOrders(@PathVariable Long materialId) {
        log.debug("Requête de calcul de la quantité totale pour la matière ID: {}", materialId);
        Integer totalQuantity = supplyOrderLineService.sumQuantityByMaterialInActiveOrders(materialId);
        return ApiResponse.success(totalQuantity);
    }

    // Calculer le montant total d'une commande
    @GetMapping("/order/{orderId}/total-amount")
    @Operation(summary = "Montant total d'une commande", description = "Calcule le montant total des lignes d'une commande")
    public ApiResponse<Double> sumTotalAmountByOrder(@PathVariable Long orderId) {
        log.debug("Requête de calcul du montant total de la commande ID: {}", orderId);
        Double totalAmount = supplyOrderLineService.sumTotalAmountByOrder(orderId);
        return ApiResponse.success(totalAmount);
    }

    // Vérifier si une matière a des lignes actives
    @GetMapping("/material/{materialId}/has-active")
    @Operation(summary = "Vérifier lignes actives", description = "Vérifie si une matière première a des lignes de commande actives")
    public ApiResponse<Boolean> materialHasActiveOrderLines(@PathVariable Long materialId) {
        log.debug("Requête de vérification des lignes actives pour la matière ID: {}", materialId);
        boolean hasActive = supplyOrderLineService.materialHasActiveOrderLines(materialId);
        return ApiResponse.success(hasActive);
    }
}
