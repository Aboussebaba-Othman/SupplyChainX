package com.supplychainx.supply.controller;

import com.supplychainx.common.dto.ApiResponse;
import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.service.SupplierService;
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
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fournisseurs", description = "API de gestion des fournisseurs")
@PreAuthorize("@securityExpressions.hasSupplyAccess()")
public class SupplierController {

    private final SupplierService supplierService;

   
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un fournisseur", description = "Crée un nouveau fournisseur dans le système")
    public ApiResponse<SupplierResponseDTO> create(@Valid @RequestBody SupplierRequestDTO requestDTO) {
        log.info("Requête de création d'un fournisseur - Code: {}", requestDTO.getCode());
        SupplierResponseDTO supplier = supplierService.create(requestDTO);
        return ApiResponse.success("Fournisseur créé avec succès", supplier);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un fournisseur", description = "Met à jour les informations d'un fournisseur existant")
    public ApiResponse<SupplierResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequestDTO requestDTO) {
        log.info("Requête de mise à jour du fournisseur ID: {}", id);
        SupplierResponseDTO supplier = supplierService.update(id, requestDTO);
        return ApiResponse.success("Fournisseur mis à jour avec succès", supplier);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un fournisseur", description = "Supprime un fournisseur s'il n'a pas de commandes actives")
    public void delete(@PathVariable Long id) {
        log.info("Requête de suppression du fournisseur ID: {}", id);
        supplierService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un fournisseur par ID", description = "Récupère les détails d'un fournisseur par son ID")
    public ApiResponse<SupplierResponseDTO> findById(@PathVariable Long id) {
        log.debug("Requête de consultation du fournisseur ID: {}", id);
        SupplierResponseDTO supplier = supplierService.findById(id);
        return ApiResponse.success(supplier);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Consulter un fournisseur par code", description = "Récupère les détails d'un fournisseur par son code")
    public ApiResponse<SupplierResponseDTO> findByCode(@PathVariable String code) {
        log.debug("Requête de consultation du fournisseur avec le code: {}", code);
        SupplierResponseDTO supplier = supplierService.findByCode(code);
        return ApiResponse.success(supplier);
    }

    @GetMapping
    @Operation(summary = "Lister tous les fournisseurs", description = "Récupère la liste de tous les fournisseurs avec pagination")
    public ApiResponse<PageResponse<SupplierResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de liste de tous les fournisseurs - Page: {}", pageable.getPageNumber());
        PageResponse<SupplierResponseDTO> suppliers = supplierService.findAll(pageable);
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des fournisseurs par nom", description = "Recherche des fournisseurs dont le nom contient la chaîne fournie")
    public ApiResponse<List<SupplierResponseDTO>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("Requête de recherche de fournisseurs - Nom: {}", name);
        List<SupplierResponseDTO> suppliers = supplierService.searchByName(name, pageable);
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/rating/{minRating}")
    @Operation(summary = "Filtrer par note minimale", description = "Récupère les fournisseurs avec une note supérieure ou égale à la note minimale")
    public ApiResponse<List<SupplierResponseDTO>> findByMinimumRating(@PathVariable Double minRating) {
        log.debug("Requête de filtrage des fournisseurs - Note minimale: {}", minRating);
        List<SupplierResponseDTO> suppliers = supplierService.findByMinimumRating(minRating);
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/lead-time/{maxLeadTime}")
    @Operation(summary = "Filtrer par délai maximum", description = "Récupère les fournisseurs avec un délai de livraison inférieur ou égal au délai maximum")
    public ApiResponse<List<SupplierResponseDTO>> findByMaxLeadTime(@PathVariable Integer maxLeadTime) {
        log.debug("Requête de filtrage des fournisseurs - Délai maximum: {} jours", maxLeadTime);
        List<SupplierResponseDTO> suppliers = supplierService.findByMaxLeadTime(maxLeadTime);
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Fournisseurs les mieux notés", description = "Récupère tous les fournisseurs triés par note décroissante")
    public ApiResponse<List<SupplierResponseDTO>> findTopRated(
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Requête de liste des fournisseurs les mieux notés");
        List<SupplierResponseDTO> suppliers = supplierService.findAllOrderedByRating(pageable);
        return ApiResponse.success(suppliers);
    }

    @GetMapping("/{id}/can-delete")
    @Operation(summary = "Vérifier la suppression possible", description = "Vérifie si un fournisseur peut être supprimé (pas de commandes actives)")
    public ApiResponse<Boolean> canBeDeleted(@PathVariable Long id) {
        log.debug("Requête de vérification de suppression du fournisseur ID: {}", id);
        boolean canDelete = supplierService.canBeDeleted(id);
        return ApiResponse.success(canDelete);
    }
}
