package com.supplychainx.production.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.mapper.ProductMapper;
import com.supplychainx.production.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // Créer un nouveau produit
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        log.info("Création d'un nouveau produit avec le code: {}", requestDTO.getCode());

        // Vérifier si le code existe déjà
        if (productRepository.existsByCode(requestDTO.getCode())) {
            throw new BusinessException("Un produit avec ce code existe déjà: " + requestDTO.getCode());
        }

        Product product = productMapper.toEntity(requestDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Produit créé avec succès - ID: {}, Code: {}", savedProduct.getId(), savedProduct.getCode());
        return productMapper.toResponseDTO(savedProduct);
    }

    // Récupérer un produit par ID
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Récupération du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        return productMapper.toResponseDTO(product);
    }

    // Récupérer un produit par code
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByCode(String code) {
        log.debug("Récupération du produit avec le code: {}", code);

        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec le code: " + code));

        return productMapper.toResponseDTO(product);
    }

    // Récupérer tous les produits (avec pagination)
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        log.debug("Récupération de tous les produits - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return productRepository.findAll(pageable)
                .map(productMapper::toResponseDTO);
    }

    // Rechercher des produits par nom
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProductsByName(String name, Pageable pageable) {
        log.debug("Recherche de produits par nom contenant: {}", name);

        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toResponseDTO);
    }

    // Récupérer les produits par catégorie
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategory(String category, Pageable pageable) {
        log.debug("Récupération des produits de la catégorie: {}", category);

        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toResponseDTO);
    }

    // Récupérer les produits en stock bas
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getLowStockProducts(Pageable pageable) {
        log.debug("Récupération des produits en stock bas");

        return productRepository.findLowStockProducts(pageable)
                .map(productMapper::toResponseDTO);
    }

    // Compter les produits en stock bas
    @Transactional(readOnly = true)
    public Long countLowStockProducts() {
        log.debug("Comptage des produits en stock bas");
        return productRepository.countLowStockProducts();
    }

    // Calculer la valeur totale de l'inventaire
    @Transactional(readOnly = true)
    public Double calculateTotalInventoryValue() {
        log.debug("Calcul de la valeur totale de l'inventaire");
        Double total = productRepository.calculateTotalInventoryValue();
        return total != null ? total : 0.0;
    }

    // Mettre à jour un produit
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        log.info("Mise à jour du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau code existe déjà (sauf si c'est le même produit)
        if (!product.getCode().equals(requestDTO.getCode()) && productRepository.existsByCode(requestDTO.getCode())) {
            throw new BusinessException("Un produit avec ce code existe déjà: " + requestDTO.getCode());
        }

        productMapper.updateEntityFromDTO(requestDTO, product);
        Product updatedProduct = productRepository.save(product);

        log.info("Produit mis à jour avec succès - ID: {}", id);
        return productMapper.toResponseDTO(updatedProduct);
    }

    // Ajouter du stock à un produit
    public ProductResponseDTO addStock(Long id, Double quantity) {
        log.info("Ajout de {} unités au stock du produit ID: {}", quantity, id);

        if (quantity == null || quantity <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        Double currentStock = product.getStock() != null ? product.getStock() : 0.0;
        product.setStock(currentStock + quantity);

        Product updatedProduct = productRepository.save(product);

        log.info("Stock ajouté avec succès - Nouveau stock: {}", updatedProduct.getStock());
        return productMapper.toResponseDTO(updatedProduct);
    }

    // Réduire le stock d'un produit
    public ProductResponseDTO reduceStock(Long id, Double quantity) {
        log.info("Réduction de {} unités du stock du produit ID: {}", quantity, id);

        if (quantity == null || quantity <= 0) {
            throw new BusinessException("La quantité doit être positive");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        Double currentStock = product.getStock() != null ? product.getStock() : 0.0;

        if (currentStock < quantity) {
            throw new BusinessException("Stock insuffisant. Stock actuel: " + currentStock + ", Quantité demandée: " + quantity);
        }

        product.setStock(currentStock - quantity);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock réduit avec succès - Nouveau stock: {}", updatedProduct.getStock());
        return productMapper.toResponseDTO(updatedProduct);
    }

    // Supprimer un produit
    public void deleteProduct(Long id) {
        log.info("Suppression du produit avec l'ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        // Vérifier si le produit est utilisé dans des ordres de production
        if (productRepository.isUsedInProductionOrders(id)) {
            throw new BusinessException("Impossible de supprimer ce produit car il est utilisé dans des ordres de production");
        }

        productRepository.delete(product);
        log.info("Produit supprimé avec succès - ID: {}", id);
    }
}
