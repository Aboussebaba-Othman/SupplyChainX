package com.supplychainx.supply.mapper;

import com.supplychainx.supply.dto.request.SupplierRequestDTO;
import com.supplychainx.supply.dto.response.SupplierResponseDTO;
import com.supplychainx.supply.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupplierMapper {

    // Convertir RequestDTO vers Entity
    Supplier toEntity(SupplierRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    SupplierResponseDTO toResponseDTO(Supplier entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<SupplierResponseDTO> toResponseDTOList(List<Supplier> entities);

    // Mettre à jour une entité existante avec les données du DTO
    void updateEntityFromDTO(SupplierRequestDTO dto, @MappingTarget Supplier entity);
}
