package com.supplychainx.supply.service;

import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.request.SupplyOrderRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.entity.Supplier;
import com.supplychainx.supply.entity.SupplyOrder;
import com.supplychainx.supply.entity.SupplyOrderLine;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import com.supplychainx.supply.mapper.SupplyOrderMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplierRepository;
import com.supplychainx.supply.repository.SupplyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplyOrderService {

    private final SupplyOrderRepository supplyOrderRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final SupplyOrderMapper supplyOrderMapper;

    // Créer une nouvelle commande d'approvisionnement
    @Transactional
    public SupplyOrderResponseDTO create(SupplyOrderRequestDTO requestDTO) {
        log.info("Création d'une nouvelle commande avec le numéro: {}", requestDTO.getOrderNumber());
        // Vérifier si le numéro de commande existe déjà
        if (supplyOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new DuplicateResourceException("Une commande avec le numéro " + requestDTO.getOrderNumber() + " existe déjà");
        }
        // Vérifier que le fournisseur existe
        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + requestDTO.getSupplierId()));
        // Créer la commande
        SupplyOrder supplyOrder = supplyOrderMapper.toEntity(requestDTO);
        supplyOrder.setSupplier(supplier);
        // Créer les lignes de commande
        List<SupplyOrderLine> orderLines = new ArrayList<>();
        for (SupplyOrderLineRequestDTO lineDTO : requestDTO.getOrderLines()) {
            RawMaterial material = rawMaterialRepository.findById(lineDTO.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + lineDTO.getMaterialId()));
            SupplyOrderLine orderLine = SupplyOrderLine.builder()
                    .supplyOrder(supplyOrder)
                    .material(material)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(lineDTO.getUnitPrice())
                    .build();
            orderLines.add(orderLine);
        }
        supplyOrder.setOrderLines(orderLines);
        SupplyOrder savedOrder = supplyOrderRepository.save(supplyOrder);
        log.info("Commande créée avec succès - ID: {}, Numéro: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        return supplyOrderMapper.toResponseDTO(savedOrder);
    }
    // Mettre à jour une commande existante
    @Transactional
    public SupplyOrderResponseDTO update(Long id, SupplyOrderRequestDTO requestDTO) {
        log.info("Mise à jour de la commande ID: {}", id);
        SupplyOrder existingOrder = supplyOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        // Vérifier si la commande peut être modifiée
        if (existingOrder.getStatus() == SupplyOrderStatus.RECUE || existingOrder.getStatus() == SupplyOrderStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier une commande avec le statut: " + existingOrder.getStatus());
        }
        // Vérifier si le nouveau numéro existe déjà (sauf pour la commande actuelle)
        if (!existingOrder.getOrderNumber().equals(requestDTO.getOrderNumber()) && 
            supplyOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new DuplicateResourceException("Une commande avec le numéro " + requestDTO.getOrderNumber() + " existe déjà");
        }
        // Vérifier que le fournisseur existe
        Supplier supplier = supplierRepository.findById(requestDTO.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + requestDTO.getSupplierId()));
        // Mettre à jour les champs de base
        supplyOrderMapper.updateEntityFromDTO(requestDTO, existingOrder);
        existingOrder.setSupplier(supplier);
        // Mettre à jour les lignes de commande
        existingOrder.getOrderLines().clear();
        List<SupplyOrderLine> newOrderLines = new ArrayList<>();
        for (SupplyOrderLineRequestDTO lineDTO : requestDTO.getOrderLines()) {
            RawMaterial material = rawMaterialRepository.findById(lineDTO.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + lineDTO.getMaterialId()));

            SupplyOrderLine orderLine = SupplyOrderLine.builder()
                    .supplyOrder(existingOrder)
                    .material(material)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(lineDTO.getUnitPrice())
                    .build();

            newOrderLines.add(orderLine);
        }
        existingOrder.getOrderLines().addAll(newOrderLines);

        SupplyOrder updatedOrder = supplyOrderRepository.save(existingOrder);

        log.info("Commande mise à jour avec succès - ID: {}", id);
        return supplyOrderMapper.toResponseDTO(updatedOrder);
    }

    // Supprimer une commande
    @Transactional
    public void delete(Long id) {
        log.info("Suppression de la commande ID: {}", id);

        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        // Vérifier si la commande peut être supprimée
        if (order.getStatus() == SupplyOrderStatus.RECUE) {
            throw new BusinessException("Impossible de supprimer une commande avec le statut RECUE");
        }

        supplyOrderRepository.delete(order);
        log.info("Commande supprimée avec succès - ID: {}", id);
    }

    // Récupérer une commande par ID
    public SupplyOrderResponseDTO findById(Long id) {
        log.debug("Recherche de la commande ID: {}", id);

        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        return supplyOrderMapper.toResponseDTO(order);
    }
    // Récupérer une commande par numéro
    public SupplyOrderResponseDTO findByOrderNumber(String orderNumber) {
        log.debug("Recherche de la commande avec le numéro: {}", orderNumber);
        SupplyOrder order = supplyOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec le numéro: " + orderNumber));
        return supplyOrderMapper.toResponseDTO(order);
    }

    // Récupérer toutes les commandes avec pagination
    public PageResponse<SupplyOrderResponseDTO> findAll(Pageable pageable) {
        log.debug("Récupération de toutes les commandes - Page: {}, Taille: {}", 
                  pageable.getPageNumber(), pageable.getPageSize());
        Page<SupplyOrder> orderPage = supplyOrderRepository.findAll(pageable);
        List<SupplyOrderResponseDTO> orders = supplyOrderMapper.toResponseDTOList(orderPage.getContent());
        return PageResponse.of(
                orders,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    // Récupérer les commandes par statut
    public List<SupplyOrderResponseDTO> findByStatus(SupplyOrderStatus status, Pageable pageable) {
        log.debug("Recherche des commandes avec le statut: {}", status);
        Page<SupplyOrder> orders = supplyOrderRepository.findByStatus(status, pageable);
        return supplyOrderMapper.toResponseDTOList(orders.getContent());
    }

    // Récupérer les commandes d'un fournisseur
    public List<SupplyOrderResponseDTO> findBySupplier(Long supplierId, Pageable pageable) {
        log.debug("Recherche des commandes du fournisseur ID: {}", supplierId);
        // Vérifier que le fournisseur existe
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId);
        }
        Page<SupplyOrder> orders = supplyOrderRepository.findBySupplierId(supplierId, pageable);
        return supplyOrderMapper.toResponseDTOList(orders.getContent());
    }

    // Récupérer les commandes d'un fournisseur par statut
    public List<SupplyOrderResponseDTO> findBySupplierAndStatus(Long supplierId, SupplyOrderStatus status, Pageable pageable) {
        log.debug("Recherche des commandes du fournisseur ID: {} avec le statut: {}", supplierId, status);
        // Vérifier que le fournisseur existe
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId);
        }
        Page<SupplyOrder> orders = supplyOrderRepository.findBySupplierIdAndStatus(supplierId, status, pageable);
        return supplyOrderMapper.toResponseDTOList(orders.getContent());
    }

    // Récupérer les commandes entre deux dates
    public List<SupplyOrderResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Recherche des commandes entre {} et {}", startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("La date de début doit être antérieure à la date de fin");
        }
        Page<SupplyOrder> orders = supplyOrderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        return supplyOrderMapper.toResponseDTOList(orders.getContent());
    }

    // Récupérer les commandes en retard
    public List<SupplyOrderResponseDTO> findDelayedOrders() {
        log.debug("Recherche des commandes en retard");
        List<SupplyOrder> orders = supplyOrderRepository.findDelayedOrders(LocalDate.now());
        return supplyOrderMapper.toResponseDTOList(orders);
    }

    // Récupérer les commandes récentes
    public List<SupplyOrderResponseDTO> findRecentOrders(Pageable pageable) {
        log.debug("Recherche des commandes récentes");
        Page<SupplyOrder> orders = supplyOrderRepository.findRecentOrders(pageable);
        return supplyOrderMapper.toResponseDTOList(orders.getContent());
    }

    // Mettre à jour le statut d'une commande
    @Transactional
    public SupplyOrderResponseDTO updateStatus(Long id, SupplyOrderStatus newStatus) {
        log.info("Mise à jour du statut de la commande ID: {} vers {}", id, newStatus);
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        // Vérifier la transition de statut
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        SupplyOrder updatedOrder = supplyOrderRepository.save(order);
        log.info("Statut de la commande mis à jour avec succès - ID: {}, Nouveau statut: {}", id, newStatus);
        return supplyOrderMapper.toResponseDTO(updatedOrder);
    }

    // Marquer une commande comme reçue
    @Transactional
    public SupplyOrderResponseDTO receiveOrder(Long id, LocalDate actualDeliveryDate) {
        log.info("Réception de la commande ID: {} à la date: {}", id, actualDeliveryDate);
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        if (order.getStatus() != SupplyOrderStatus.EN_COURS) {
            throw new BusinessException("Seules les commandes EN_COURS peuvent être marquées comme reçues");
        }
        order.setStatus(SupplyOrderStatus.RECUE);
        order.setActualDeliveryDate(actualDeliveryDate);
        // Mettre à jour le stock des matières premières
        for (SupplyOrderLine line : order.getOrderLines()) {
            RawMaterial material = line.getMaterial();
            material.setStock(material.getStock() + line.getQuantity());
            rawMaterialRepository.save(material);
            log.info("Stock mis à jour pour la matière {} - Quantité ajoutée: {}", material.getCode(), line.getQuantity());
        }
        SupplyOrder updatedOrder = supplyOrderRepository.save(order);
        log.info("Commande reçue avec succès - ID: {}, Date de réception: {}", id, actualDeliveryDate);
        return supplyOrderMapper.toResponseDTO(updatedOrder);
    }

    // Annuler une commande
    @Transactional
    public SupplyOrderResponseDTO cancelOrder(Long id) {
        log.info("Annulation de la commande ID: {}", id);
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        if (order.getStatus() == SupplyOrderStatus.RECUE) {
            throw new BusinessException("Impossible d'annuler une commande déjà reçue");
        }
        if (order.getStatus() == SupplyOrderStatus.ANNULEE) {
            throw new BusinessException("La commande est déjà annulée");
        }
        order.setStatus(SupplyOrderStatus.ANNULEE);
        SupplyOrder updatedOrder = supplyOrderRepository.save(order);
        log.info("Commande annulée avec succès - ID: {}", id);
        return supplyOrderMapper.toResponseDTO(updatedOrder);
    }

    // Compter les commandes actives d'un fournisseur
    public Long countActiveOrdersBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId);
        }
        return supplyOrderRepository.countActiveOrdersBySupplier(supplierId);
    }

    // Calculer le montant total par statut
    public Double sumTotalAmountByStatus(SupplyOrderStatus status) {
        return supplyOrderRepository.sumTotalAmountByStatus(status);
    }

    // Vérifier si une commande peut être supprimée
    public boolean canBeDeleted(Long id) {
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return order.getStatus() != SupplyOrderStatus.RECUE;
    }

    // Vérifier si une commande peut être modifiée
    public boolean canBeModified(Long id) {
        SupplyOrder order = supplyOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));
        return order.getStatus() != SupplyOrderStatus.RECUE && order.getStatus() != SupplyOrderStatus.ANNULEE;
    }

    // Valider la transition de statut
    private void validateStatusTransition(SupplyOrderStatus currentStatus, SupplyOrderStatus newStatus) {
        if (currentStatus == SupplyOrderStatus.RECUE) {
            throw new BusinessException("Impossible de modifier le statut d'une commande déjà reçue");
        }

        if (currentStatus == SupplyOrderStatus.ANNULEE && newStatus != SupplyOrderStatus.EN_ATTENTE) {
            throw new BusinessException("Une commande annulée ne peut être réactivée qu'en statut EN_ATTENTE");
        }

        if (currentStatus == SupplyOrderStatus.EN_ATTENTE && newStatus == SupplyOrderStatus.RECUE) {
            throw new BusinessException("Une commande EN_ATTENTE doit passer par le statut EN_COURS avant d'être reçue");
        }
    }
}
