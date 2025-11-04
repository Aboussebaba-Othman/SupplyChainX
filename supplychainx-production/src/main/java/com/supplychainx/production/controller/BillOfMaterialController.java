package com.supplychainx.production.controller;

import com.supplychainx.production.dto.request.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.response.BillOfMaterialResponseDTO;
import com.supplychainx.production.service.BillOfMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/production/bills-of-material")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bills of Material", description = "API de gestion des nomenclatures (recettes de production)")
@PreAuthorize("@securityExpressions.hasProductionAccess()")
public class BillOfMaterialController {

    private final BillOfMaterialService billOfMaterialService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle nomenclature", description = "Crée une nouvelle nomenclature liant un produit à une matière première")
    public ResponseEntity<BillOfMaterialResponseDTO> createBillOfMaterial(@Valid @RequestBody BillOfMaterialRequestDTO requestDTO) {
        log.info("Requête de création de nomenclature pour produit ID: {} et matière première ID: {}", 
                 requestDTO.getProductId(), requestDTO.getRawMaterialId());
        BillOfMaterialResponseDTO response = billOfMaterialService.createBillOfMaterial(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une nomenclature par ID", description = "Récupère les détails d'une nomenclature par son identifiant")
    public ResponseEntity<BillOfMaterialResponseDTO> getBillOfMaterialById(@PathVariable Long id) {
        log.info("Requête de récupération de la nomenclature ID: {}", id);
        BillOfMaterialResponseDTO response = billOfMaterialService.getBillOfMaterialById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les nomenclatures", description = "Récupère la liste de toutes les nomenclatures")
    public ResponseEntity<List<BillOfMaterialResponseDTO>> getAllBillOfMaterials() {
        log.info("Requête de récupération de toutes les nomenclatures");
        List<BillOfMaterialResponseDTO> response = billOfMaterialService.getAllBillOfMaterials();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Récupérer les nomenclatures par produit", description = "Récupère toutes les nomenclatures (recette) d'un produit spécifique")
    public ResponseEntity<List<BillOfMaterialResponseDTO>> getBillOfMaterialsByProduct(@PathVariable Long productId) {
        log.info("Requête de récupération des nomenclatures pour le produit ID: {}", productId);
        List<BillOfMaterialResponseDTO> response = billOfMaterialService.getBillOfMaterialsByProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/raw-material/{rawMaterialId}")
    @Operation(summary = "Récupérer les nomenclatures par matière première", description = "Récupère toutes les nomenclatures utilisant une matière première spécifique")
    public ResponseEntity<List<BillOfMaterialResponseDTO>> getBillOfMaterialsByRawMaterial(@PathVariable Long rawMaterialId) {
        log.info("Requête de récupération des nomenclatures pour la matière première ID: {}", rawMaterialId);
        List<BillOfMaterialResponseDTO> response = billOfMaterialService.getBillOfMaterialsByRawMaterial(rawMaterialId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}/total-quantity")
    @Operation(summary = "Calculer la quantité totale de matières premières", description = "Calcule la quantité totale de matières premières nécessaires pour un produit")
    public ResponseEntity<Map<String, Double>> getTotalRawMaterialQuantityForProduct(@PathVariable Long productId) {
        log.info("Requête de calcul de la quantité totale de matières premières pour le produit ID: {}", productId);
        Double totalQuantity = billOfMaterialService.getTotalRawMaterialQuantityForProduct(productId);
        return ResponseEntity.ok(Map.of("totalQuantity", totalQuantity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une nomenclature", description = "Met à jour les informations d'une nomenclature existante")
    public ResponseEntity<BillOfMaterialResponseDTO> updateBillOfMaterial(
            @PathVariable Long id,
            @Valid @RequestBody BillOfMaterialRequestDTO requestDTO) {
        log.info("Requête de mise à jour de la nomenclature ID: {}", id);
        BillOfMaterialResponseDTO response = billOfMaterialService.updateBillOfMaterial(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une nomenclature", description = "Supprime une nomenclature")
    public ResponseEntity<Void> deleteBillOfMaterial(@PathVariable Long id) {
        log.info("Requête de suppression de la nomenclature ID: {}", id);
        billOfMaterialService.deleteBillOfMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
