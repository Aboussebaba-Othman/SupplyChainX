package com.supplychainx.production.controller;

import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/production/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "API de gestion des produits finis")
@PreAuthorize("@securityExpressions.hasProductionAccess()")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Créer un nouveau produit", description = "Crée un nouveau produit avec validation du code unique")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        log.info("Requête de création de produit: {}", requestDTO.getCode());
        ProductResponseDTO response = productService.createProduct(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID", description = "Récupère les détails d'un produit par son identifiant")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        log.info("Requête de récupération du produit ID: {}", id);
        ProductResponseDTO response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Récupérer un produit par code", description = "Récupère les détails d'un produit par son code unique")
    public ResponseEntity<ProductResponseDTO> getProductByCode(@PathVariable String code) {
        log.info("Requête de récupération du produit avec le code: {}", code);
        ProductResponseDTO response = productService.getProductByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Récupérer tous les produits", description = "Récupère la liste paginée de tous les produits")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Requête de récupération de tous les produits - Page: {}, Size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductResponseDTO> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des produits par nom", description = "Recherche des produits dont le nom contient la chaîne spécifiée")
    public ResponseEntity<Page<ProductResponseDTO>> searchProductsByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Requête de recherche de produits par nom: {}", name);
        Page<ProductResponseDTO> response = productService.searchProductsByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Récupérer les produits par catégorie", description = "Récupère tous les produits d'une catégorie spécifique")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        log.info("Requête de récupération des produits de la catégorie: {}", category);
        Page<ProductResponseDTO> response = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Récupérer les produits en stock bas", description = "Récupère les produits dont le stock est inférieur au stock minimum")
    public ResponseEntity<Page<ProductResponseDTO>> getLowStockProducts(
            @PageableDefault(size = 20, sort = "stock") Pageable pageable) {
        log.info("Requête de récupération des produits en stock bas");
        Page<ProductResponseDTO> response = productService.getLowStockProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock/count")
    @Operation(summary = "Compter les produits en stock bas", description = "Retourne le nombre de produits en stock bas")
    public ResponseEntity<Map<String, Long>> countLowStockProducts() {
        log.info("Requête de comptage des produits en stock bas");
        Long count = productService.countLowStockProducts();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/inventory/value")
    @Operation(summary = "Calculer la valeur totale de l'inventaire", description = "Calcule la valeur totale de tous les produits en stock")
    public ResponseEntity<Map<String, Double>> calculateTotalInventoryValue() {
        log.info("Requête de calcul de la valeur totale de l'inventaire");
        Double totalValue = productService.calculateTotalInventoryValue();
        return ResponseEntity.ok(Map.of("totalValue", totalValue));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un produit", description = "Met à jour les informations d'un produit existant")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO requestDTO) {
        log.info("Requête de mise à jour du produit ID: {}", id);
        ProductResponseDTO response = productService.updateProduct(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/add-stock")
    @Operation(summary = "Ajouter du stock", description = "Ajoute une quantité au stock d'un produit")
    public ResponseEntity<ProductResponseDTO> addStock(
            @PathVariable Long id,
            @RequestParam Double quantity) {
        log.info("Requête d'ajout de stock pour le produit ID: {} - Quantité: {}", id, quantity);
        ProductResponseDTO response = productService.addStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reduce-stock")
    @Operation(summary = "Réduire le stock", description = "Réduit le stock d'un produit (avec validation de disponibilité)")
    public ResponseEntity<ProductResponseDTO> reduceStock(
            @PathVariable Long id,
            @RequestParam Double quantity) {
        log.info("Requête de réduction de stock pour le produit ID: {} - Quantité: {}", id, quantity);
        ProductResponseDTO response = productService.reduceStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit", description = "Supprime un produit (si non utilisé dans des ordres de production)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Requête de suppression du produit ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
