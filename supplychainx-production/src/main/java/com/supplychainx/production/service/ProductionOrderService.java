package com.supplychainx.production.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.request.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.response.ProductionOrderResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.entity.ProductionOrder;
import com.supplychainx.production.enums.ProductionOrderStatus;
import com.supplychainx.production.mapper.ProductionOrderMapper;
import com.supplychainx.production.repository.BillOfMaterialRepository;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.production.repository.ProductionOrderRepository;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductionOrderService {

    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final ProductionOrderMapper productionOrderMapper;

    // Créer un nouvel ordre de production
    public ProductionOrderResponseDTO createProductionOrder(ProductionOrderRequestDTO requestDTO) {
        log.info("Création d'un nouvel ordre de production: {}", requestDTO.getOrderNumber());

        // Vérifier si le numéro d'ordre existe déjà
        if (productionOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new BusinessException("Un ordre de production avec ce numéro existe déjà: " + requestDTO.getOrderNumber());
        }

        // Vérifier que le produit existe
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + requestDTO.getProductId()));

        ProductionOrder productionOrder = productionOrderMapper.toEntity(requestDTO);
        productionOrder.setProduct(product);
        productionOrder.setStatus(ProductionOrderStatus.EN_ATTENTE);

        // Calculer le temps estimé
        Integer estimatedTime = productionOrder.calculateEstimatedTime();
        productionOrder.setEstimatedTime(estimatedTime);

        ProductionOrder savedOrder = productionOrderRepository.save(productionOrder);

        log.info("Ordre de production créé avec succès - ID: {}, Numéro: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        return productionOrderMapper.toResponseDTO(savedOrder);
    }

    // Récupérer un ordre de production par ID
    @Transactional(readOnly = true)
    public ProductionOrderResponseDTO getProductionOrderById(Long id) {
        log.debug("Récupération de l'ordre de production avec l'ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        return productionOrderMapper.toResponseDTO(productionOrder);
    }

    // Récupérer un ordre de production par numéro
    @Transactional(readOnly = true)
    public ProductionOrderResponseDTO getProductionOrderByOrderNumber(String orderNumber) {
        log.debug("Récupération de l'ordre de production avec le numéro: {}", orderNumber);

        ProductionOrder productionOrder = productionOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec le numéro: " + orderNumber));

        return productionOrderMapper.toResponseDTO(productionOrder);
    }

    // Récupérer tous les ordres de production (avec pagination)
    @Transactional(readOnly = true)
    public Page<ProductionOrderResponseDTO> getAllProductionOrders(Pageable pageable) {
        log.debug("Récupération de tous les ordres de production - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return productionOrderRepository.findAll(pageable)
                .map(productionOrderMapper::toResponseDTO);
    }

    // Récupérer les ordres de production par statut
    @Transactional(readOnly = true)
    public Page<ProductionOrderResponseDTO> getProductionOrdersByStatus(ProductionOrderStatus status, Pageable pageable) {
        log.debug("Récupération des ordres de production avec le statut: {}", status);

        return productionOrderRepository.findByStatus(status, pageable)
                .map(productionOrderMapper::toResponseDTO);
    }

    // Récupérer les ordres de production par produit
    @Transactional(readOnly = true)
    public Page<ProductionOrderResponseDTO> getProductionOrdersByProduct(Long productId, Pageable pageable) {
        log.debug("Récupération des ordres de production pour le produit ID: {}", productId);

        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId);
        }

        return productionOrderRepository.findByProductId(productId, pageable)
                .map(productionOrderMapper::toResponseDTO);
    }

    // Récupérer les ordres de production retardés
    @Transactional(readOnly = true)
    public List<ProductionOrderResponseDTO> getDelayedProductionOrders() {
        log.debug("Récupération des ordres de production retardés");

        List<ProductionOrder> delayedOrders = productionOrderRepository.findByStatusAndEndDateBefore(ProductionOrderStatus.EN_PRODUCTION, LocalDate.now());
        return productionOrderMapper.toResponseDTOList(delayedOrders);
    }

    // Récupérer les ordres de production par période
    @Transactional(readOnly = true)
    public Page<ProductionOrderResponseDTO> getProductionOrdersByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Récupération des ordres de production entre {} et {}", startDate, endDate);

    // Use startDate as the period field (plannedDate was removed/renamed to startDate)
    return productionOrderRepository.findByStartDateBetween(startDate, endDate, pageable)
        .map(productionOrderMapper::toResponseDTO);
    }

    // Mettre à jour un ordre de production
    public ProductionOrderResponseDTO updateProductionOrder(Long id, ProductionOrderRequestDTO requestDTO) {
        log.info("Mise à jour de l'ordre de production avec l'ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        // Vérifier si l'ordre peut être modifié
        if (productionOrder.getStatus() == ProductionOrderStatus.TERMINE || 
            productionOrder.getStatus() == ProductionOrderStatus.ANNULE) {
            throw new BusinessException("Impossible de modifier un ordre de production terminé ou annulé");
        }

        // Vérifier si le nouveau numéro d'ordre existe déjà (sauf si c'est le même ordre)
        if (!productionOrder.getOrderNumber().equals(requestDTO.getOrderNumber()) && 
            productionOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new BusinessException("Un ordre de production avec ce numéro existe déjà: " + requestDTO.getOrderNumber());
        }

        // Vérifier que le produit existe
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + requestDTO.getProductId()));

        productionOrderMapper.updateEntityFromDTO(requestDTO, productionOrder);
        productionOrder.setProduct(product);

        // Recalculer le temps estimé
        Integer estimatedTime = productionOrder.calculateEstimatedTime();
        productionOrder.setEstimatedTime(estimatedTime);

        ProductionOrder updatedOrder = productionOrderRepository.save(productionOrder);

        log.info("Ordre de production mis à jour avec succès - ID: {}", id);
        return productionOrderMapper.toResponseDTO(updatedOrder);
    }

    // Démarrer la production
    public ProductionOrderResponseDTO startProduction(Long id) {
        log.info("Démarrage de la production pour l'ordre ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        // Vérifier que l'ordre est au statut EN_ATTENTE
        if (productionOrder.getStatus() != ProductionOrderStatus.EN_ATTENTE) {
            throw new BusinessException("Seuls les ordres en attente peuvent être démarrés. Statut actuel: " + productionOrder.getStatus());
        }

        // Vérifier la disponibilité des matières premières
        if (!checkRawMaterialsAvailability(productionOrder)) {
            throw new BusinessException("Matières premières insuffisantes pour démarrer la production");
        }

        // Mettre à jour le statut et la date de début
        productionOrder.setStatus(ProductionOrderStatus.EN_PRODUCTION);
        productionOrder.setStartDate(LocalDate.now());

        ProductionOrder updatedOrder = productionOrderRepository.save(productionOrder);

        log.info("Production démarrée avec succès pour l'ordre ID: {}", id);
        return productionOrderMapper.toResponseDTO(updatedOrder);
    }

    // Terminer la production
    public ProductionOrderResponseDTO completeProduction(Long id) {
        log.info("Finalisation de la production pour l'ordre ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        // Vérifier que l'ordre est au statut EN_PRODUCTION
        if (productionOrder.getStatus() != ProductionOrderStatus.EN_PRODUCTION) {
            throw new BusinessException("Seuls les ordres en production peuvent être terminés. Statut actuel: " + productionOrder.getStatus());
        }

        // Consommer les matières premières
        consumeRawMaterials(productionOrder);

        // Ajouter les produits finis au stock
        Product product = productionOrder.getProduct();
        Double currentStock = product.getStock() != null ? product.getStock() : 0.0;
        product.setStock(currentStock + productionOrder.getQuantity());
        productRepository.save(product);

        // Mettre à jour le statut et la date de fin
        productionOrder.setStatus(ProductionOrderStatus.TERMINE);
        productionOrder.setEndDate(LocalDate.now());

        ProductionOrder updatedOrder = productionOrderRepository.save(productionOrder);

        log.info("Production terminée avec succès pour l'ordre ID: {} - {} unités ajoutées au stock", 
                 id, productionOrder.getQuantity());
        return productionOrderMapper.toResponseDTO(updatedOrder);
    }

    // Annuler un ordre de production
    public ProductionOrderResponseDTO cancelProductionOrder(Long id) {
        log.info("Annulation de l'ordre de production ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        // Vérifier que l'ordre n'est pas déjà terminé
        if (productionOrder.getStatus() == ProductionOrderStatus.TERMINE) {
            throw new BusinessException("Impossible d'annuler un ordre de production terminé");
        }

        // Vérifier que l'ordre n'est pas déjà annulé
        if (productionOrder.getStatus() == ProductionOrderStatus.ANNULE) {
            throw new BusinessException("Cet ordre de production est déjà annulé");
        }

        productionOrder.setStatus(ProductionOrderStatus.ANNULE);
        ProductionOrder updatedOrder = productionOrderRepository.save(productionOrder);

        log.info("Ordre de production annulé avec succès - ID: {}", id);
        return productionOrderMapper.toResponseDTO(updatedOrder);
    }

    // Supprimer un ordre de production
    public void deleteProductionOrder(Long id) {
        log.info("Suppression de l'ordre de production avec l'ID: {}", id);

        ProductionOrder productionOrder = productionOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de production non trouvé avec l'ID: " + id));

        // Seuls les ordres annulés ou planifiés peuvent être supprimés
        if (productionOrder.getStatus() == ProductionOrderStatus.EN_PRODUCTION || 
            productionOrder.getStatus() == ProductionOrderStatus.TERMINE) {
            throw new BusinessException("Impossible de supprimer un ordre en cours ou terminé. Veuillez d'abord l'annuler.");
        }

        productionOrderRepository.delete(productionOrder);
        log.info("Ordre de production supprimé avec succès - ID: {}", id);
    }

    // Vérifier la disponibilité des matières premières
    private boolean checkRawMaterialsAvailability(ProductionOrder productionOrder) {
        log.debug("Vérification de la disponibilité des matières premières pour l'ordre ID: {}", productionOrder.getId());

        Product product = productionOrder.getProduct();
        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductId(product.getId());

        for (BillOfMaterial bom : billOfMaterials) {
            RawMaterial rawMaterial = bom.getRawMaterial();
            Double requiredQuantity = bom.getQuantity() * productionOrder.getQuantity();
            
            if (rawMaterial.getStock() < requiredQuantity) {
                log.warn("Stock insuffisant pour la matière première {} - Requis: {}, Disponible: {}", 
                         rawMaterial.getName(), requiredQuantity, rawMaterial.getStock());
                return false;
            }
        }

        return true;
    }

    // Consommer les matières premières
    private void consumeRawMaterials(ProductionOrder productionOrder) {
        log.info("Consommation des matières premières pour l'ordre ID: {}", productionOrder.getId());

        Product product = productionOrder.getProduct();
        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductId(product.getId());

        for (BillOfMaterial bom : billOfMaterials) {
            RawMaterial rawMaterial = bom.getRawMaterial();
            Double requiredQuantity = bom.getQuantity() * productionOrder.getQuantity();

            if (rawMaterial.getStock() < requiredQuantity) {
                throw new BusinessException("Stock insuffisant pour la matière première: " + rawMaterial.getName());
            }

            // Réduire le stock de la matière première
            // RawMaterial.stock is Integer, so round requiredQuantity up
            rawMaterial.setStock(rawMaterial.getStock() - (int) Math.ceil(requiredQuantity));
            rawMaterialRepository.save(rawMaterial);

            log.debug("Matière première consommée: {} - Quantité: {}", rawMaterial.getName(), requiredQuantity);
        }

        log.info("Matières premières consommées avec succès pour l'ordre ID: {}", productionOrder.getId());
    }
}
