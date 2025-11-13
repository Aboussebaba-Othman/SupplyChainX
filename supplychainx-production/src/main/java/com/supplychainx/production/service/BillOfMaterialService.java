package com.supplychainx.production.service;

import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.production.dto.request.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.response.BillOfMaterialResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.production.entity.Product;
import com.supplychainx.production.mapper.BillOfMaterialMapper;
import com.supplychainx.production.repository.BillOfMaterialRepository;
import com.supplychainx.production.repository.ProductRepository;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillOfMaterialService {

    private final BillOfMaterialRepository billOfMaterialRepository;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final BillOfMaterialMapper billOfMaterialMapper;

    // Créer une nouvelle nomenclature
    public BillOfMaterialResponseDTO createBillOfMaterial(BillOfMaterialRequestDTO requestDTO) {
        log.info("Création d'une nouvelle nomenclature pour le produit ID: {} et matière première ID: {}", 
                 requestDTO.getProductId(), requestDTO.getRawMaterialId());

        // Vérifier que le produit existe
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + requestDTO.getProductId()));

        // Vérifier que la matière première existe
        RawMaterial rawMaterial = rawMaterialRepository.findById(requestDTO.getRawMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + requestDTO.getRawMaterialId()));

        BillOfMaterial billOfMaterial = billOfMaterialMapper.toEntity(requestDTO);
        billOfMaterial.setProduct(product);
        billOfMaterial.setRawMaterial(rawMaterial);

        BillOfMaterial savedBillOfMaterial = billOfMaterialRepository.save(billOfMaterial);

        log.info("Nomenclature créée avec succès - ID: {}", savedBillOfMaterial.getId());
        return billOfMaterialMapper.toResponseDTO(savedBillOfMaterial);
    }

    // Récupérer une nomenclature par ID
    @Transactional(readOnly = true)
    public BillOfMaterialResponseDTO getBillOfMaterialById(Long id) {
        log.debug("Récupération de la nomenclature avec l'ID: {}", id);

        BillOfMaterial billOfMaterial = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nomenclature non trouvée avec l'ID: " + id));

        return billOfMaterialMapper.toResponseDTO(billOfMaterial);
    }

    // Récupérer toutes les nomenclatures
    @Transactional(readOnly = true)
    public List<BillOfMaterialResponseDTO> getAllBillOfMaterials() {
        log.debug("Récupération de toutes les nomenclatures");

        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findAll();
        return billOfMaterialMapper.toResponseDTOList(billOfMaterials);
    }

    // Récupérer les nomenclatures par produit
    @Transactional(readOnly = true)
    public List<BillOfMaterialResponseDTO> getBillOfMaterialsByProduct(Long productId) {
        log.debug("Récupération des nomenclatures pour le produit ID: {}", productId);

        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId);
        }

        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByProductId(productId);
        return billOfMaterialMapper.toResponseDTOList(billOfMaterials);
    }

    // Récupérer les nomenclatures par matière première
    @Transactional(readOnly = true)
    public List<BillOfMaterialResponseDTO> getBillOfMaterialsByRawMaterial(Long rawMaterialId) {
        log.debug("Récupération des nomenclatures pour la matière première ID: {}", rawMaterialId);

        // Vérifier que la matière première existe
        if (!rawMaterialRepository.existsById(rawMaterialId)) {
            throw new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + rawMaterialId);
        }

        List<BillOfMaterial> billOfMaterials = billOfMaterialRepository.findByRawMaterialId(rawMaterialId);
        return billOfMaterialMapper.toResponseDTOList(billOfMaterials);
    }

    // Calculer la quantité totale d'une matière première nécessaire pour un produit
    @Transactional(readOnly = true)
    public Double getTotalRawMaterialQuantityForProduct(Long productId) {
        log.debug("Calcul de la quantité totale de matières premières pour le produit ID: {}", productId);

        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + productId);
        }

        Double total = billOfMaterialRepository.sumQuantityByProductId(productId);
        return total != null ? total : 0.0;
    }

    // Mettre à jour une nomenclature
    public BillOfMaterialResponseDTO updateBillOfMaterial(Long id, BillOfMaterialRequestDTO requestDTO) {
        log.info("Mise à jour de la nomenclature avec l'ID: {}", id);

        BillOfMaterial billOfMaterial = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nomenclature non trouvée avec l'ID: " + id));

        // Vérifier que le produit existe
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + requestDTO.getProductId()));

        // Vérifier que la matière première existe
        RawMaterial rawMaterial = rawMaterialRepository.findById(requestDTO.getRawMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + requestDTO.getRawMaterialId()));

        billOfMaterialMapper.updateEntityFromDTO(requestDTO, billOfMaterial);
        billOfMaterial.setProduct(product);
        billOfMaterial.setRawMaterial(rawMaterial);

        BillOfMaterial updatedBillOfMaterial = billOfMaterialRepository.save(billOfMaterial);

        log.info("Nomenclature mise à jour avec succès - ID: {}", id);
        return billOfMaterialMapper.toResponseDTO(updatedBillOfMaterial);
    }

    // Supprimer une nomenclature
    public void deleteBillOfMaterial(Long id) {
        log.info("Suppression de la nomenclature avec l'ID: {}", id);

        BillOfMaterial billOfMaterial = billOfMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nomenclature non trouvée avec l'ID: " + id));

        billOfMaterialRepository.delete(billOfMaterial);
        log.info("Nomenclature supprimée avec succès - ID: {}", id);
    }
}
