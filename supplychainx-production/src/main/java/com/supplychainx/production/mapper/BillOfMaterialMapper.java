package com.supplychainx.production.mapper;

import com.supplychainx.production.dto.request.BillOfMaterialRequestDTO;
import com.supplychainx.production.dto.response.BillOfMaterialResponseDTO;
import com.supplychainx.production.entity.BillOfMaterial;
import com.supplychainx.supply.mapper.RawMaterialMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProductMapper.class, RawMaterialMapper.class})
public interface BillOfMaterialMapper {

    // Convertir RequestDTO vers Entity (product et rawMaterial gérés manuellement dans le service)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "rawMaterial", ignore = true)
    BillOfMaterial toEntity(BillOfMaterialRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "lineCost", expression = "java(entity.calculateTotalCost())")
    BillOfMaterialResponseDTO toResponseDTO(BillOfMaterial entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<BillOfMaterialResponseDTO> toResponseDTOList(List<BillOfMaterial> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "rawMaterial", ignore = true)
    void updateEntityFromDTO(BillOfMaterialRequestDTO dto, @MappingTarget BillOfMaterial entity);
}
