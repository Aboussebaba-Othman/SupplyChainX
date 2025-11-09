package com.supplychainx.supply.controller;

import com.supplychainx.common.dto.ApiResponse;
import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.supply.dto.request.RawMaterialRequestDTO;
import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import com.supplychainx.supply.service.RawMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/raw-materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Matières Premières", description = "API de gestion des matières premières")
public class RawMaterialController {

    private final RawMaterialService rawMaterialService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une matière première", description = "Crée une nouvelle matière première dans le système")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_CREATE')")
    public ApiResponse<RawMaterialResponseDTO> create(@Valid @RequestBody RawMaterialRequestDTO requestDTO) {
        log.info("Requête de création d'une matière première - Code: {}", requestDTO.getCode());
        RawMaterialResponseDTO material = rawMaterialService.create(requestDTO);
        return ApiResponse.success("Matière première créée avec succès", material);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une matière première", description = "Met à jour les informations d'une matière première existante")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_UPDATE')")
    public ApiResponse<RawMaterialResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RawMaterialRequestDTO requestDTO) {
        log.info("Requête de mise à jour de la matière première ID: {}", id);
        RawMaterialResponseDTO material = rawMaterialService.update(id, requestDTO);
        return ApiResponse.success("Matière première mise à jour avec succès", material);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une matière première", description = "Supprime une matière première si elle n'est pas utilisée dans des commandes")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_DELETE')")
    public void delete(@PathVariable Long id) {
        log.info("Requête de suppression de la matière première ID: {}", id);
        rawMaterialService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une matière première par ID", description = "Récupère les détails d'une matière première par son ID")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<RawMaterialResponseDTO> findById(@PathVariable Long id) {
        log.debug("Requête de consultation de la matière première ID: {}", id);
        RawMaterialResponseDTO material = rawMaterialService.findById(id);
        return ApiResponse.success(material);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Consulter une matière première par code", description = "Récupère les détails d'une matière première par son code")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<RawMaterialResponseDTO> findByCode(@PathVariable String code) {
        log.debug("Requête de consultation de la matière première avec le code: {}", code);
        RawMaterialResponseDTO material = rawMaterialService.findByCode(code);
        return ApiResponse.success(material);
    }

    @GetMapping
    @Operation(summary = "Lister toutes les matières premières", description = "Récupère la liste de toutes les matières premières avec pagination")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<PageResponse<RawMaterialResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de liste de toutes les matières premières - Page: {}", pageable.getPageNumber());
        PageResponse<RawMaterialResponseDTO> materials = rawMaterialService.findAll(pageable);
        return ApiResponse.success(materials);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des matières premières par nom", description = "Recherche des matières premières dont le nom contient la chaîne fournie")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<List<RawMaterialResponseDTO>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de recherche de matières premières - Nom: {}", name);
        List<RawMaterialResponseDTO> materials = rawMaterialService.searchByName(name, pageable);
        return ApiResponse.success(materials);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filtrer par catégorie", description = "Récupère les matières premières d'une catégorie spécifique")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<List<RawMaterialResponseDTO>> findByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de filtrage des matières premières - Catégorie: {}", category);
        List<RawMaterialResponseDTO> materials = rawMaterialService.findByCategory(category, pageable);
        return ApiResponse.success(materials);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Matières en stock faible", description = "Récupère les matières premières dont le stock est inférieur au stock minimum")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<List<RawMaterialResponseDTO>> findLowStockMaterials() {
        log.debug("Requête de liste des matières en stock faible");
        List<RawMaterialResponseDTO> materials = rawMaterialService.findLowStockMaterials();
        return ApiResponse.success(materials);
    }

    @GetMapping("/low-stock/paginated")
    @Operation(summary = "Matières en stock faible avec pagination", description = "Récupère les matières premières en stock faible avec pagination")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<PageResponse<RawMaterialResponseDTO>> findLowStockMaterialsPaginated(
            @PageableDefault(size = 20, sort = "stock", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de liste paginée des matières en stock faible");
        PageResponse<RawMaterialResponseDTO> materials = rawMaterialService.findLowStockMaterialsPaginated(pageable);
        return ApiResponse.success(materials);
    }

    @GetMapping("/low-stock/count")
    @Operation(summary = "Compter les matières en stock faible", description = "Retourne le nombre de matières premières en stock faible")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<Long> countLowStockMaterials() {
        log.debug("Requête de comptage des matières en stock faible");
        Long count = rawMaterialService.countLowStockMaterials();
        return ApiResponse.success(count);
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Matières d'un fournisseur", description = "Récupère les matières premières fournies par un fournisseur spécifique")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<List<RawMaterialResponseDTO>> findBySupplier(@PathVariable Long supplierId) {
        log.debug("Requête de liste des matières du fournisseur ID: {}", supplierId);
        List<RawMaterialResponseDTO> materials = rawMaterialService.findBySupplier(supplierId);
        return ApiResponse.success(materials);
    }

    // Ajouter du stock
    @PatchMapping("/{id}/add-stock")
    @Operation(summary = "Ajouter du stock", description = "Ajoute une quantité au stock d'une matière première")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_UPDATE')")
    public ApiResponse<RawMaterialResponseDTO> addStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("Requête d'ajout de stock - Matière ID: {}, Quantité: {}", id, quantity);
        RawMaterialResponseDTO material = rawMaterialService.addStock(id, quantity);
        return ApiResponse.success("Stock ajouté avec succès", material);
    }

    // Réduire le stock
    @PatchMapping("/{id}/reduce-stock")
    @Operation(summary = "Réduire le stock", description = "Réduit le stock d'une matière première")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_UPDATE')")
    public ApiResponse<RawMaterialResponseDTO> reduceStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        log.info("Requête de réduction de stock - Matière ID: {}, Quantité: {}", id, quantity);
        RawMaterialResponseDTO material = rawMaterialService.reduceStock(id, quantity);
        return ApiResponse.success("Stock réduit avec succès", material);
    }

    // Vérifier si une matière première peut être supprimée
    @GetMapping("/{id}/can-delete")
    @Operation(summary = "Vérifier la suppression possible", description = "Vérifie si une matière première peut être supprimée (pas utilisée dans des commandes)")
    @PreAuthorize("@securityExpressions.hasPermission('RAW_MATERIAL_READ')")
    public ApiResponse<Boolean> canBeDeleted(@PathVariable Long id) {
        log.debug("Requête de vérification de suppression de la matière première ID: {}", id);
        boolean canDelete = rawMaterialService.canBeDeleted(id);
        return ApiResponse.success(canDelete);
    }
}
