package com.supplychainx.supply.service;

import com.supplychainx.common.dto.PageResponse;
import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.common.exception.DuplicateResourceException;
import com.supplychainx.common.exception.ResourceNotFoundException;
import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.entity.Supplier;
import com.supplychainx.supply.mapper.SupplierMapper;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponseDTO create(SupplierRequestDTO requestDTO) {
        log.info("Création d'un nouveau fournisseur avec le code: {}", requestDTO.getCode());
        if (supplierRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Un fournisseur avec le code " + requestDTO.getCode() + " existe déjà");
        }
        if (requestDTO.getEmail() != null && supplierRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Un fournisseur avec l'email " + requestDTO.getEmail() + " existe déjà");
        }
        Supplier supplier = supplierMapper.toEntity(requestDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Fournisseur créé avec succès - ID: {}, Code: {}", savedSupplier.getId(), savedSupplier.getCode());
        return supplierMapper.toResponseDTO(savedSupplier);
    }

    @Transactional
    public SupplierResponseDTO update(Long id, SupplierRequestDTO requestDTO) {
        log.info("Mise à jour du fournisseur ID: {}", id);
        Supplier existingSupplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        if (!existingSupplier.getCode().equals(requestDTO.getCode()) && 
            supplierRepository.existsByCode(requestDTO.getCode())) {
            throw new DuplicateResourceException("Un fournisseur avec le code " + requestDTO.getCode() + " existe déjà");
        }
        if (requestDTO.getEmail() != null && 
            !requestDTO.getEmail().equals(existingSupplier.getEmail()) && 
            supplierRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Un fournisseur avec l'email " + requestDTO.getEmail() + " existe déjà");
        }
        supplierMapper.updateEntityFromDTO(requestDTO, existingSupplier);
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        log.info("Fournisseur mis à jour avec succès - ID: {}", id);
        return supplierMapper.toResponseDTO(updatedSupplier);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Suppression du fournisseur ID: {}", id);
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        if (supplierRepository.hasActiveOrders(id)) {
            throw new BusinessException("Impossible de supprimer le fournisseur car il a des commandes actives");
        }
        supplierRepository.delete(supplier);
        log.info("Fournisseur supprimé avec succès - ID: {}", id);
    }

    public SupplierResponseDTO findById(Long id) {
        log.debug("Recherche du fournisseur ID: {}", id);
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id));
        return supplierMapper.toResponseDTO(supplier);
    }

    public SupplierResponseDTO findByCode(String code) {
        log.debug("Recherche du fournisseur avec le code: {}", code);
        Supplier supplier = supplierRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec le code: " + code));
        return supplierMapper.toResponseDTO(supplier);
    }

    public PageResponse<SupplierResponseDTO> findAll(Pageable pageable) {
        log.debug("Récupération de tous les fournisseurs - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Supplier> supplierPage = supplierRepository.findAll(pageable);
        List<SupplierResponseDTO> suppliers = supplierMapper.toResponseDTOList(supplierPage.getContent());

        return PageResponse.of(
                suppliers,
                supplierPage.getNumber(),
                supplierPage.getSize(),
                supplierPage.getTotalElements(),
                supplierPage.getTotalPages()
        );
    }

    public List<SupplierResponseDTO> searchByName(String name, Pageable pageable) {
        log.debug("Recherche des fournisseurs contenant: {}", name);
        Page<Supplier> suppliers = supplierRepository.findByNameContainingIgnoreCase(name, pageable);
        return supplierMapper.toResponseDTOList(suppliers.getContent());
    }

    public List<SupplierResponseDTO> findByMinimumRating(Double minRating) {
        log.debug("Recherche des fournisseurs avec une note >= {}", minRating);
        List<Supplier> suppliers = supplierRepository.findByRatingGreaterThanEqual(minRating);
        return supplierMapper.toResponseDTOList(suppliers);
    }

    public List<SupplierResponseDTO> findByMaxLeadTime(Integer maxLeadTime) {
        log.debug("Recherche des fournisseurs avec un délai <= {}", maxLeadTime);
        List<Supplier> suppliers = supplierRepository.findByLeadTimeLessThanEqual(maxLeadTime);
        return supplierMapper.toResponseDTOList(suppliers);
    }

    public List<SupplierResponseDTO> findAllOrderedByRating(Pageable pageable) {
        log.debug("Récupération de tous les fournisseurs triés par note");
        Page<Supplier> suppliers = supplierRepository.findAllOrderByRatingDesc(pageable);
        return supplierMapper.toResponseDTOList(suppliers.getContent());
    }

    public boolean canBeDeleted(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + id);
        }
        return !supplierRepository.hasActiveOrders(id);
    }
}
