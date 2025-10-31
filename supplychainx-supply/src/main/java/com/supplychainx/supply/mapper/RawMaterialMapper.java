package com.supplychainx.supply.mapper;

import com.supplychainx.supply.dto.request.RawMaterialRequestDTO;
import com.supplychainx.supply.dto.response.RawMaterialResponseDTO;
import com.supplychainx.supply.entity.RawMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {SupplierMapper.class})
public interface RawMaterialMapper {

    // Convertir RequestDTO vers Entity (ignore supplierIds car géré manuellement dans le service)
    @Mapping(target = "suppliers", ignore = true)
    RawMaterial toEntity(RawMaterialRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "lowStock", expression = "java(entity.isLowStock())")
    RawMaterialResponseDTO toResponseDTO(RawMaterial entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<RawMaterialResponseDTO> toResponseDTOList(List<RawMaterial> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "suppliers", ignore = true)
    void updateEntityFromDTO(RawMaterialRequestDTO dto, @MappingTarget RawMaterial entity);
}
