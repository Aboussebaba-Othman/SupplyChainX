package com.supplychainx.supply.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderLineResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.entity.SupplyOrder;
import com.supplychainx.supply.entity.SupplyOrderLine;
import com.supplychainx.supply.enums.SupplyOrderStatus;
import com.supplychainx.supply.mapper.SupplyOrderLineMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplyOrderLineRepository;
import com.supplychainx.supply.repository.SupplyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupplyOrderLineService {

    private final SupplyOrderLineRepository supplyOrderLineRepository;
    private final SupplyOrderRepository supplyOrderRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final SupplyOrderLineMapper supplyOrderLineMapper;

    // Créer une nouvelle ligne de commande
    @Transactional
    public SupplyOrderLineResponseDTO create(Long orderId, SupplyOrderLineRequestDTO requestDTO) {
        log.info("Création d'une nouvelle ligne de commande pour la commande ID: {}", orderId);
        // Vérifier que la commande existe
        SupplyOrder order = supplyOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId));
        // Vérifier que la commande peut être modifiée
        if (order.getStatus() == SupplyOrderStatus.RECUE || order.getStatus() == SupplyOrderStatus.ANNULEE) {
            throw new BusinessException("Impossible d'ajouter une ligne à une commande avec le statut: " + order.getStatus());
        }
        // Vérifier que la matière première existe
        RawMaterial material = rawMaterialRepository.findById(requestDTO.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + requestDTO.getMaterialId()));
        // Créer la ligne de commande
        SupplyOrderLine orderLine = supplyOrderLineMapper.toEntity(requestDTO);
        orderLine.setSupplyOrder(order);
        orderLine.setMaterial(material);
        SupplyOrderLine savedLine = supplyOrderLineRepository.save(orderLine);
        log.info("Ligne de commande créée avec succès - ID: {}, Commande ID: {}, Matière: {}", 
                 savedLine.getId(), orderId, material.getCode());
        return supplyOrderLineMapper.toResponseDTO(savedLine);
    }

    // Mettre à jour une ligne de commande existante
    @Transactional
    public SupplyOrderLineResponseDTO update(Long lineId, SupplyOrderLineRequestDTO requestDTO) {
        log.info("Mise à jour de la ligne de commande ID: {}", lineId);
        SupplyOrderLine existingLine = supplyOrderLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de commande non trouvée avec l'ID: " + lineId));
        // Vérifier que la commande peut être modifiée
        SupplyOrder order = existingLine.getSupplyOrder();
        if (order.getStatus() == SupplyOrderStatus.RECUE || order.getStatus() == SupplyOrderStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier une ligne d'une commande avec le statut: " + order.getStatus());
        }
        // Mettre à jour la matière première si elle a changé
        if (!existingLine.getMaterial().getId().equals(requestDTO.getMaterialId())) {
            RawMaterial material = rawMaterialRepository.findById(requestDTO.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + requestDTO.getMaterialId()));
            existingLine.setMaterial(material);
        }

        // Mettre à jour les autres champs
        supplyOrderLineMapper.updateEntityFromDTO(requestDTO, existingLine);
        SupplyOrderLine updatedLine = supplyOrderLineRepository.save(existingLine);
        log.info("Ligne de commande mise à jour avec succès - ID: {}", lineId);
        return supplyOrderLineMapper.toResponseDTO(updatedLine);
    }

    // Supprimer une ligne de commande
    @Transactional
    public void delete(Long lineId) {
        log.info("Suppression de la ligne de commande ID: {}", lineId);
        SupplyOrderLine line = supplyOrderLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de commande non trouvée avec l'ID: " + lineId));
        // Vérifier que la commande peut être modifiée
        SupplyOrder order = line.getSupplyOrder();
        if (order.getStatus() == SupplyOrderStatus.RECUE) {
            throw new BusinessException("Impossible de supprimer une ligne d'une commande avec le statut RECUE");
        }
        // Vérifier qu'il reste au moins une autre ligne dans la commande
        List<SupplyOrderLine> orderLines = supplyOrderLineRepository.findBySupplyOrderId(order.getId());
        if (orderLines.size() <= 1) {
            throw new BusinessException("Impossible de supprimer la dernière ligne de la commande. Supprimez la commande entière.");
        }
        supplyOrderLineRepository.delete(line);
        log.info("Ligne de commande supprimée avec succès - ID: {}", lineId);
    }

    // Récupérer une ligne de commande par ID
    public SupplyOrderLineResponseDTO findById(Long lineId) {
        log.debug("Recherche de la ligne de commande ID: {}", lineId);
        SupplyOrderLine line = supplyOrderLineRepository.findById(lineId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de commande non trouvée avec l'ID: " + lineId));
        return supplyOrderLineMapper.toResponseDTO(line);
    }

    // Récupérer toutes les lignes d'une commande
    public List<SupplyOrderLineResponseDTO> findBySupplyOrder(Long orderId) {
        log.debug("Recherche des lignes de la commande ID: {}", orderId);
        // Vérifier que la commande existe
        if (!supplyOrderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId);
        }
        List<SupplyOrderLine> lines = supplyOrderLineRepository.findBySupplyOrderId(orderId);
        return supplyOrderLineMapper.toResponseDTOList(lines);
    }
    // Récupérer toutes les lignes contenant une matière première
    public List<SupplyOrderLineResponseDTO> findByMaterial(Long materialId) {
        log.debug("Recherche des lignes de commande pour la matière première ID: {}", materialId);
        // Vérifier que la matière première existe
        if (!rawMaterialRepository.existsById(materialId)) {
            throw new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + materialId);
        }
        List<SupplyOrderLine> lines = supplyOrderLineRepository.findByMaterialId(materialId);
        return supplyOrderLineMapper.toResponseDTOList(lines);
    }

    // Récupérer une ligne spécifique d'une commande pour une matière
    public SupplyOrderLineResponseDTO findBySupplyOrderAndMaterial(Long orderId, Long materialId) {
        log.debug("Recherche de la ligne pour la commande ID: {} et la matière ID: {}", orderId, materialId);
        List<SupplyOrderLine> lines = supplyOrderLineRepository.findBySupplyOrderIdAndMaterialId(orderId, materialId);
        if (lines.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Ligne de commande non trouvée pour la commande ID: " + orderId + " et la matière ID: " + materialId);
        }
        // Retourner la première ligne trouvée (normalement il ne devrait y en avoir qu'une)
        return supplyOrderLineMapper.toResponseDTO(lines.get(0));
    }

    // Calculer la quantité totale commandée pour une matière dans les commandes actives
    public Integer sumQuantityByMaterialInActiveOrders(Long materialId) {
        log.debug("Calcul de la quantité totale commandée pour la matière ID: {}", materialId);

        // Vérifier que la matière première existe
        if (!rawMaterialRepository.existsById(materialId)) {
            throw new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + materialId);
        }
        return supplyOrderLineRepository.sumQuantityByMaterialInActiveOrders(materialId);
    }

    // Calculer le montant total d'une commande
    public Double sumTotalAmountByOrder(Long orderId) {
        log.debug("Calcul du montant total de la commande ID: {}", orderId);
        if (!supplyOrderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId);
        }
        return supplyOrderLineRepository.sumTotalAmountByOrder(orderId);
    }

    // Vérifier si une matière première a des lignes actives
    public boolean materialHasActiveOrderLines(Long materialId) {
        if (!rawMaterialRepository.existsById(materialId)) {
            throw new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + materialId);
        }
        return supplyOrderLineRepository.materialHasActiveOrderLines(materialId);
    }

    // Supprimer toutes les lignes d'une commande (utilisé en interne)
    @Transactional
    public void deleteBySupplyOrder(Long orderId) {
        log.info("Suppression de toutes les lignes de la commande ID: {}", orderId);

        // Vérifier que la commande existe
        if (!supplyOrderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId);
        }

        supplyOrderLineRepository.deleteBySupplyOrderId(orderId);
        log.info("Toutes les lignes de la commande ID: {} ont été supprimées", orderId);
    }
}
