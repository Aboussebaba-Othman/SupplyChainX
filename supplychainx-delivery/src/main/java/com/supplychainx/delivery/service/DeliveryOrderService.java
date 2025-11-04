package com.supplychainx.delivery.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.delivery.dto.request.DeliveryOrderLineRequestDTO;
import com.supplychainx.delivery.dto.request.DeliveryOrderRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryOrderResponseDTO;
import com.supplychainx.delivery.entity.Customer;
import com.supplychainx.delivery.entity.DeliveryOrder;
import com.supplychainx.delivery.entity.DeliveryOrderLine;
import com.supplychainx.delivery.enums.OrderStatus;
import com.supplychainx.delivery.mapper.DeliveryOrderMapper;
import com.supplychainx.delivery.repository.CustomerRepository;
import com.supplychainx.delivery.repository.DeliveryOrderRepository;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.repository.ProductRepository;
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
public class DeliveryOrderService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final DeliveryOrderMapper deliveryOrderMapper;

    // Créer une nouvelle commande
    @Transactional
    public DeliveryOrderResponseDTO create(DeliveryOrderRequestDTO requestDTO) {
        log.info("Création d'une nouvelle commande avec le numéro: {}", requestDTO.getOrderNumber());

        // Vérifier si le numéro de commande existe déjà
        if (deliveryOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new DuplicateResourceException("Une commande avec le numéro " + requestDTO.getOrderNumber() + " existe déjà");
        }

        // Vérifier que le client existe
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + requestDTO.getCustomerId()));

        // Créer la commande
        DeliveryOrder deliveryOrder = deliveryOrderMapper.toEntity(requestDTO);
        deliveryOrder.setCustomer(customer);

        // Créer les lignes de commande et vérifier la disponibilité des produits
        List<DeliveryOrderLine> orderLines = new ArrayList<>();
        for (DeliveryOrderLineRequestDTO lineDTO : requestDTO.getOrderLines()) {
            Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + lineDTO.getProductId()));

            // Vérifier la disponibilité du stock
            if (!product.isAvailable(lineDTO.getQuantity())) {
                throw new BusinessException("Stock insuffisant pour le produit: " + product.getName() +
                        " (disponible: " + product.getStock() + ", demandé: " + lineDTO.getQuantity() + ")");
            }

            DeliveryOrderLine orderLine = DeliveryOrderLine.builder()
                    .deliveryOrder(deliveryOrder)
                    .product(product)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(lineDTO.getUnitPrice())
                    .build();

            orderLines.add(orderLine);
        }

        deliveryOrder.setOrderLines(orderLines);
        
        // Définir le statut initial si non fourni
        if (deliveryOrder.getStatus() == null) {
            deliveryOrder.setStatus(OrderStatus.EN_PREPARATION);
        }

        DeliveryOrder savedOrder = deliveryOrderRepository.save(deliveryOrder);

        log.info("Commande créée avec succès - ID: {}, Numéro: {}", savedOrder.getId(), savedOrder.getOrderNumber());
        return deliveryOrderMapper.toResponseDTO(savedOrder);
    }

    // Mettre à jour une commande existante
    @Transactional
    public DeliveryOrderResponseDTO update(Long id, DeliveryOrderRequestDTO requestDTO) {
        log.info("Mise à jour de la commande ID: {}", id);

        DeliveryOrder existingOrder = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        // Vérifier si la commande peut être modifiée
        if (existingOrder.getStatus() == OrderStatus.LIVREE) {
            throw new BusinessException("Impossible de modifier une commande déjà livrée");
        }

        // Vérifier si le nouveau numéro existe déjà (sauf pour la commande actuelle)
        if (!existingOrder.getOrderNumber().equals(requestDTO.getOrderNumber()) &&
                deliveryOrderRepository.existsByOrderNumber(requestDTO.getOrderNumber())) {
            throw new DuplicateResourceException("Une commande avec le numéro " + requestDTO.getOrderNumber() + " existe déjà");
        }

        // Vérifier que le client existe
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'ID: " + requestDTO.getCustomerId()));

        // Mettre à jour les champs de base
        deliveryOrderMapper.updateEntityFromDTO(requestDTO, existingOrder);
        existingOrder.setCustomer(customer);

        // Mettre à jour les lignes de commande
        existingOrder.getOrderLines().clear();
        List<DeliveryOrderLine> newOrderLines = new ArrayList<>();

        for (DeliveryOrderLineRequestDTO lineDTO : requestDTO.getOrderLines()) {
            Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + lineDTO.getProductId()));

            // Vérifier la disponibilité du stock
            if (!product.isAvailable(lineDTO.getQuantity())) {
                throw new BusinessException("Stock insuffisant pour le produit: " + product.getName() +
                        " (disponible: " + product.getStock() + ", demandé: " + lineDTO.getQuantity() + ")");
            }

            DeliveryOrderLine orderLine = DeliveryOrderLine.builder()
                    .deliveryOrder(existingOrder)
                    .product(product)
                    .quantity(lineDTO.getQuantity())
                    .unitPrice(lineDTO.getUnitPrice())
                    .build();

            newOrderLines.add(orderLine);
        }

        existingOrder.getOrderLines().addAll(newOrderLines);

        DeliveryOrder updatedOrder = deliveryOrderRepository.save(existingOrder);

        log.info("Commande mise à jour avec succès - ID: {}", id);
        return deliveryOrderMapper.toResponseDTO(updatedOrder);
    }

    // Récupérer une commande par ID
    public DeliveryOrderResponseDTO getById(Long id) {
        log.info("Récupération de la commande ID: {}", id);

        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        return deliveryOrderMapper.toResponseDTO(order);
    }

    // Récupérer une commande par numéro
    public DeliveryOrderResponseDTO getByOrderNumber(String orderNumber) {
        log.info("Récupération de la commande avec le numéro: {}", orderNumber);

        DeliveryOrder order = deliveryOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec le numéro: " + orderNumber));

        return deliveryOrderMapper.toResponseDTO(order);
    }

    // Récupérer toutes les commandes avec pagination
    public Page<DeliveryOrderResponseDTO> getAll(Pageable pageable) {
        log.info("Récupération de toutes les commandes - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<DeliveryOrder> orders = deliveryOrderRepository.findAll(pageable);
        return orders.map(deliveryOrderMapper::toResponseDTO);
    }

    // Récupérer les commandes par statut
    public Page<DeliveryOrderResponseDTO> getByStatus(OrderStatus status, Pageable pageable) {
        log.info("Récupération des commandes avec le statut: {}", status);

        Page<DeliveryOrder> orders = deliveryOrderRepository.findByStatus(status, pageable);
        return orders.map(deliveryOrderMapper::toResponseDTO);
    }

    // Récupérer les commandes d'un client
    public Page<DeliveryOrderResponseDTO> getByCustomer(Long customerId, Pageable pageable) {
        log.info("Récupération des commandes du client ID: {}", customerId);

        // Vérifier que le client existe
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Client non trouvé avec l'ID: " + customerId);
        }

        Page<DeliveryOrder> orders = deliveryOrderRepository.findByCustomerId(customerId, pageable);
        return orders.map(deliveryOrderMapper::toResponseDTO);
    }

    // Récupérer les commandes entre deux dates
    public Page<DeliveryOrderResponseDTO> getByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Récupération des commandes entre {} et {}", startDate, endDate);

        Page<DeliveryOrder> orders = deliveryOrderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        return orders.map(deliveryOrderMapper::toResponseDTO);
    }

    // Récupérer les commandes en retard
    public List<DeliveryOrderResponseDTO> getDelayedOrders() {
        log.info("Récupération des commandes en retard");

        List<DeliveryOrder> orders = deliveryOrderRepository.findDelayedOrders(OrderStatus.EN_ROUTE, LocalDate.now());
        return deliveryOrderMapper.toResponseDTOList(orders);
    }

    // Changer le statut d'une commande
    @Transactional
    public DeliveryOrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {
        log.info("Changement du statut de la commande ID: {} vers {}", id, newStatus);

        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        // Vérifier les transitions de statut valides
        if (order.getStatus() == OrderStatus.LIVREE) {
            throw new BusinessException("Impossible de modifier le statut d'une commande déjà livrée");
        }

        if (order.getStatus() == OrderStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier le statut d'une commande annulée");
        }

        order.setStatus(newStatus);

        // Si la commande est livrée, mettre à jour la date de livraison
        if (newStatus == OrderStatus.LIVREE) {
            order.setActualDeliveryDate(LocalDate.now());
        }

        DeliveryOrder updatedOrder = deliveryOrderRepository.save(order);

        log.info("Statut de la commande mis à jour - ID: {}, Nouveau statut: {}", id, newStatus);
        return deliveryOrderMapper.toResponseDTO(updatedOrder);
    }

    // Supprimer une commande
    @Transactional
    public void delete(Long id) {
        log.info("Suppression de la commande ID: {}", id);

        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + id));

        // Vérifier si la commande peut être supprimée
        if (order.getStatus() == OrderStatus.EN_ROUTE || order.getStatus() == OrderStatus.LIVREE) {
            throw new BusinessException("Impossible de supprimer une commande en cours de livraison ou déjà livrée");
        }

        deliveryOrderRepository.delete(order);
        log.info("Commande supprimée avec succès - ID: {}", id);
    }
}
