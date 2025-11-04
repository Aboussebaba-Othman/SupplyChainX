package com.supplychainx.delivery.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.delivery.dto.request.DeliveryRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryResponseDTO;
import com.supplychainx.delivery.entity.Delivery;
import com.supplychainx.delivery.entity.DeliveryOrder;
import com.supplychainx.delivery.enums.DeliveryStatus;
import com.supplychainx.delivery.mapper.DeliveryMapper;
import com.supplychainx.delivery.repository.DeliveryOrderRepository;
import com.supplychainx.delivery.repository.DeliveryRepository;
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
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final DeliveryMapper deliveryMapper;

    // Créer une nouvelle livraison
    @Transactional
    public DeliveryResponseDTO create(DeliveryRequestDTO requestDTO) {
        log.info("Création d'une nouvelle livraison avec le numéro: {}", requestDTO.getDeliveryNumber());

        // Vérifier si le numéro de livraison existe déjà
        if (deliveryRepository.existsByDeliveryNumber(requestDTO.getDeliveryNumber())) {
            throw new DuplicateResourceException("Une livraison avec le numéro " + requestDTO.getDeliveryNumber() + " existe déjà");
        }

        // Vérifier que la commande existe
        DeliveryOrder deliveryOrder = deliveryOrderRepository.findById(requestDTO.getDeliveryOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + requestDTO.getDeliveryOrderId()));

        // Vérifier que la commande n'a pas déjà une livraison associée
        if (deliveryRepository.findByDeliveryOrderId(requestDTO.getDeliveryOrderId()).isPresent()) {
            throw new BusinessException("Cette commande a déjà une livraison associée");
        }

        // Créer la livraison
        Delivery delivery = deliveryMapper.toEntity(requestDTO);
        delivery.setDeliveryOrder(deliveryOrder);

        // Définir le statut initial si non fourni
        if (delivery.getStatus() == null) {
            delivery.setStatus(DeliveryStatus.PLANIFIEE);
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);

        log.info("Livraison créée avec succès - ID: {}, Numéro: {}", savedDelivery.getId(), savedDelivery.getDeliveryNumber());
        return deliveryMapper.toResponseDTO(savedDelivery);
    }

    // Mettre à jour une livraison existante
    @Transactional
    public DeliveryResponseDTO update(Long id, DeliveryRequestDTO requestDTO) {
        log.info("Mise à jour de la livraison ID: {}", id);

        Delivery existingDelivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));

        // Vérifier si la livraison peut être modifiée
        if (!existingDelivery.canBeModified()) {
            throw new BusinessException("Impossible de modifier une livraison avec le statut: " + existingDelivery.getStatus());
        }

        // Vérifier si le nouveau numéro existe déjà (sauf pour la livraison actuelle)
        if (!existingDelivery.getDeliveryNumber().equals(requestDTO.getDeliveryNumber()) &&
                deliveryRepository.existsByDeliveryNumber(requestDTO.getDeliveryNumber())) {
            throw new DuplicateResourceException("Une livraison avec le numéro " + requestDTO.getDeliveryNumber() + " existe déjà");
        }

        // Vérifier que la commande existe
        DeliveryOrder deliveryOrder = deliveryOrderRepository.findById(requestDTO.getDeliveryOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + requestDTO.getDeliveryOrderId()));

        // Mettre à jour les champs
        deliveryMapper.updateEntityFromDTO(requestDTO, existingDelivery);
        existingDelivery.setDeliveryOrder(deliveryOrder);

        Delivery updatedDelivery = deliveryRepository.save(existingDelivery);

        log.info("Livraison mise à jour avec succès - ID: {}", id);
        return deliveryMapper.toResponseDTO(updatedDelivery);
    }

    // Récupérer une livraison par ID
    public DeliveryResponseDTO getById(Long id) {
        log.info("Récupération de la livraison ID: {}", id);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));

        return deliveryMapper.toResponseDTO(delivery);
    }

    // Récupérer une livraison par numéro
    public DeliveryResponseDTO getByDeliveryNumber(String deliveryNumber) {
        log.info("Récupération de la livraison avec le numéro: {}", deliveryNumber);

        Delivery delivery = deliveryRepository.findByDeliveryNumber(deliveryNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec le numéro: " + deliveryNumber));

        return deliveryMapper.toResponseDTO(delivery);
    }

    // Récupérer une livraison par numéro de suivi
    public DeliveryResponseDTO getByTrackingNumber(String trackingNumber) {
        log.info("Récupération de la livraison avec le numéro de suivi: {}", trackingNumber);

        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec le numéro de suivi: " + trackingNumber));

        return deliveryMapper.toResponseDTO(delivery);
    }

    // Récupérer la livraison d'une commande
    public DeliveryResponseDTO getByDeliveryOrder(Long deliveryOrderId) {
        log.info("Récupération de la livraison pour la commande ID: {}", deliveryOrderId);

        Delivery delivery = deliveryRepository.findByDeliveryOrderId(deliveryOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune livraison trouvée pour la commande ID: " + deliveryOrderId));

        return deliveryMapper.toResponseDTO(delivery);
    }

    // Récupérer toutes les livraisons avec pagination
    public Page<DeliveryResponseDTO> getAll(Pageable pageable) {
        log.info("Récupération de toutes les livraisons - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Delivery> deliveries = deliveryRepository.findAll(pageable);
        return deliveries.map(deliveryMapper::toResponseDTO);
    }

    // Récupérer les livraisons par statut
    public Page<DeliveryResponseDTO> getByStatus(DeliveryStatus status, Pageable pageable) {
        log.info("Récupération des livraisons avec le statut: {}", status);

        Page<Delivery> deliveries = deliveryRepository.findByStatus(status, pageable);
        return deliveries.map(deliveryMapper::toResponseDTO);
    }

    // Récupérer les livraisons par date
    public List<DeliveryResponseDTO> getByDeliveryDate(LocalDate date) {
        log.info("Récupération des livraisons prévues pour le: {}", date);

        List<Delivery> deliveries = deliveryRepository.findByDeliveryDate(date);
        return deliveryMapper.toResponseDTOList(deliveries);
    }

    // Récupérer les livraisons en retard
    public List<DeliveryResponseDTO> getDelayedDeliveries() {
        log.info("Récupération des livraisons en retard");

        List<Delivery> deliveries = deliveryRepository.findDelayedDeliveries(DeliveryStatus.EN_COURS, LocalDate.now());
        return deliveryMapper.toResponseDTOList(deliveries);
    }

    // Récupérer les livraisons actives d'un chauffeur
    public List<DeliveryResponseDTO> getActiveDeliveriesByDriver(String driver) {
        log.info("Récupération des livraisons actives du chauffeur: {}", driver);

        List<Delivery> deliveries = deliveryRepository.findActiveDeliveriesByDriver(driver);
        return deliveryMapper.toResponseDTOList(deliveries);
    }

    // Changer le statut d'une livraison
    @Transactional
    public DeliveryResponseDTO updateStatus(Long id, DeliveryStatus newStatus) {
        log.info("Changement du statut de la livraison ID: {} vers {}", id, newStatus);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));

        // Vérifier les transitions de statut valides
        if (delivery.getStatus() == DeliveryStatus.LIVREE) {
            throw new BusinessException("Impossible de modifier le statut d'une livraison déjà effectuée");
        }

        if (delivery.getStatus() == DeliveryStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier le statut d'une livraison annulée");
        }

        delivery.setStatus(newStatus);

        // Si la livraison est effectuée, mettre à jour la date de livraison
        if (newStatus == DeliveryStatus.LIVREE) {
            delivery.markAsDelivered();
        }

        Delivery updatedDelivery = deliveryRepository.save(delivery);

        log.info("Statut de la livraison mis à jour - ID: {}, Nouveau statut: {}", id, newStatus);
        return deliveryMapper.toResponseDTO(updatedDelivery);
    }

    // Marquer une livraison comme livrée
    @Transactional
    public DeliveryResponseDTO markAsDelivered(Long id) {
        log.info("Marquage de la livraison ID: {} comme livrée", id);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));

        if (delivery.getStatus() == DeliveryStatus.LIVREE) {
            throw new BusinessException("Cette livraison est déjà marquée comme livrée");
        }

        delivery.markAsDelivered();
        Delivery updatedDelivery = deliveryRepository.save(delivery);

        log.info("Livraison marquée comme livrée - ID: {}", id);
        return deliveryMapper.toResponseDTO(updatedDelivery);
    }

    // Supprimer une livraison
    @Transactional
    public void delete(Long id) {
        log.info("Suppression de la livraison ID: {}", id);

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livraison non trouvée avec l'ID: " + id));

        // Vérifier si la livraison peut être supprimée
        if (!delivery.canBeModified()) {
            throw new BusinessException("Impossible de supprimer une livraison avec le statut: " + delivery.getStatus());
        }

        deliveryRepository.delete(delivery);
        log.info("Livraison supprimée avec succès - ID: {}", id);
    }
}
