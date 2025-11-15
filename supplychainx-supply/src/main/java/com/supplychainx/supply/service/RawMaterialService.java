package com.supplychainx.supply.service;

import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.InsufficientStockException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.RawMaterialRequestDTO;
import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import com.supplychainx.supply.entity.Supplier;
import com.supplychainx.supply.mapper.RawMaterialMapper;
import com.supplychainx.supply.repository.RawMaterialRepository;
import com.supplychainx.supply.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RawMaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final SupplierRepository supplierRepository;
    private final RawMaterialMapper rawMaterialMapper;

    @Transactional
    public RawMaterialResponseDTO create(RawMaterialRequestDTO requestDTO) {
        log.info("Création d'une nouvelle matière première avec le code: {}", requestDTO.getCode());
        if (rawMaterialRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Une matière première avec le code " + requestDTO.getCode() + " existe déjà");
        }
        RawMaterial rawMaterial = rawMaterialMapper.toEntity(requestDTO);
        if (requestDTO.getSupplierIds() != null && !requestDTO.getSupplierIds().isEmpty()) {
            List<Supplier> suppliers = new java.util.ArrayList<>();
            for (Long supplierId : requestDTO.getSupplierIds()) {
                Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId));
                suppliers.add(supplier);
            }
            rawMaterial.setSuppliers(suppliers);
        }
        RawMaterial savedMaterial = rawMaterialRepository.save(rawMaterial);
        log.info("Matière première créée avec succès - ID: {}, Code: {}", savedMaterial.getId(), savedMaterial.getCode());
        return rawMaterialMapper.toResponseDTO(savedMaterial);
    }

    @Transactional
    public RawMaterialResponseDTO update(Long id, RawMaterialRequestDTO requestDTO) {
        log.info("Mise à jour de la matière première ID: {}", id);
        RawMaterial existingMaterial = rawMaterialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id));
        if (!existingMaterial.getCode().equals(requestDTO.getCode()) &&
            rawMaterialRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Une matière première avec le code " + requestDTO.getCode() + " existe déjà");
        }
        rawMaterialMapper.updateEntityFromDTO(requestDTO, existingMaterial);
        if (requestDTO.getSupplierIds() != null) {
            List<Supplier> suppliers = new java.util.ArrayList<>();
            for (Long supplierId : requestDTO.getSupplierIds()) {
                Supplier supplier = supplierRepository.findById(supplierId).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId));
                suppliers.add(supplier);
            }
            existingMaterial.setSuppliers(suppliers);
        }
        RawMaterial updatedMaterial = rawMaterialRepository.save(existingMaterial);
        log.info("Matière première mise à jour avec succès - ID: {}", id);
        return rawMaterialMapper.toResponseDTO(updatedMaterial);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Suppression de la matière première ID: {}", id);
        RawMaterial material = rawMaterialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id));
        if (rawMaterialRepository.isUsedInOrders(id)) {
            throw new BusinessException("Impossible de supprimer la matière première car elle est utilisée dans des commandes");
        }
        rawMaterialRepository.delete(material);
        log.info("Matière première supprimée avec succès - ID: {}", id);
    }

    public RawMaterialResponseDTO findById(Long id) {
        log.debug("Recherche de la matière première ID: {}", id);
        RawMaterial material = rawMaterialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id));
        return rawMaterialMapper.toResponseDTO(material);
    }

    public RawMaterialResponseDTO findByCode(String code) {
        log.debug("Recherche de la matière première avec le code: {}", code);
        RawMaterial material = rawMaterialRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec le code: " + code));
        return rawMaterialMapper.toResponseDTO(material);
    }

    public PageResponse<RawMaterialResponseDTO> findAll(Pageable pageable) {
        log.debug("Récupération de toutes les matières premières - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<RawMaterial> materialPage = rawMaterialRepository.findAll(pageable);
        List<RawMaterialResponseDTO> materials = rawMaterialMapper.toResponseDTOList(materialPage.getContent());
        return PageResponse.of(
                materials,
                materialPage.getNumber(),
                materialPage.getSize(),
                materialPage.getTotalElements(),
                materialPage.getTotalPages()
        );
    }

    public List<RawMaterialResponseDTO> searchByName(String name, Pageable pageable) {
        log.debug("Recherche des matières premières contenant: {}", name);
        Page<RawMaterial> materials = rawMaterialRepository.findByNameContainingIgnoreCase(name, pageable);
        return rawMaterialMapper.toResponseDTOList(materials.getContent());
    }

    public List<RawMaterialResponseDTO> findByCategory(String category, Pageable pageable) {
        log.debug("Recherche des matières premières de catégorie: {}", category);
        Page<RawMaterial> materials = rawMaterialRepository.findByCategory(category, pageable);
        return rawMaterialMapper.toResponseDTOList(materials.getContent());
    }

    public List<RawMaterialResponseDTO> findLowStockMaterials() {
        log.debug("Recherche des matières premières en stock faible");
        List<RawMaterial> materials = rawMaterialRepository.findLowStockMaterials();
        return rawMaterialMapper.toResponseDTOList(materials);
    }

    public PageResponse<RawMaterialResponseDTO> findLowStockMaterialsPaginated(Pageable pageable) {
        log.debug("Recherche des matières premières en stock faible - Page: {}", pageable.getPageNumber());
        Page<RawMaterial> materialPage = rawMaterialRepository.findLowStockMaterials(pageable);
        List<RawMaterialResponseDTO> materials = rawMaterialMapper.toResponseDTOList(materialPage.getContent());
        return PageResponse.of(
                materials,
                materialPage.getNumber(),
                materialPage.getSize(),
                materialPage.getTotalElements(),
                materialPage.getTotalPages()
        );
    }

    public Long countLowStockMaterials() {
        log.debug("Comptage des matières premières en stock faible");
        return rawMaterialRepository.countLowStockMaterials();
    }
    public List<RawMaterialResponseDTO> findBySupplier(Long supplierId) {
        log.debug("Recherche des matières premières du fournisseur ID: {}", supplierId);
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + supplierId);
        }
        List<RawMaterial> materials = rawMaterialRepository.findBySupplier(supplierId);
        return rawMaterialMapper.toResponseDTOList(materials);
    }

    @Transactional
    public RawMaterialResponseDTO addStock(Long id, int quantity) {
        log.info("Ajout de {} unités au stock de la matière première ID: {}", quantity, id);
        if (quantity == 0 || quantity <= 0) {
            throw new BusinessException("La quantité à ajouter doit être supérieure à 0");
        }
        RawMaterial material = rawMaterialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id));
        material.setStock(material.getStock() + quantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);
        log.info("Stock ajouté avec succès - ID: {}, Nouveau stock: {}", id, updatedMaterial.getStock());
        return rawMaterialMapper.toResponseDTO(updatedMaterial);
    }

    @Transactional
    public RawMaterialResponseDTO reduceStock(Long id, Integer quantity) {
        log.info("Réduction de {} unités du stock de la matière première ID: {}", quantity, id);
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("La quantité à réduire doit être supérieure à 0");
        }
        RawMaterial material = rawMaterialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id));
        if (material.getStock() < quantity) {
            log.error("Stock insuffisant pour la matière première ID: {}", id);
            throw new InsufficientStockException("Stock insuffisant. Stock actuel: " + material.getStock() + ", quantité demandée: " + quantity);
        }
        material.setStock(material.getStock() - quantity);
        RawMaterial updatedMaterial = rawMaterialRepository.save(material);
        log.info("Stock réduit avec succès - ID: {}, Nouveau stock: {}", id, updatedMaterial.getStock());
        return rawMaterialMapper.toResponseDTO(updatedMaterial);
    }

    public boolean canBeDeleted(Long id) {
        if (!rawMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Matière première non trouvée avec l'ID: " + id);
        }
        return !rawMaterialRepository.isUsedInOrders(id);
    }
}
