package com.supplychainx.supply.mapper;

import com.supplychainx.supply.dto.request.SupplyOrderLineRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderLineResponseDTO;
import com.supplychainx.supply.entity.SupplyOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {RawMaterialMapper.class})
public interface SupplyOrderLineMapper {

    // Convertir RequestDTO vers Entity (material géré manuellement dans le service)
    @Mapping(target = "material", ignore = true)
    @Mapping(target = "supplyOrder", ignore = true)
    SupplyOrderLine toEntity(SupplyOrderLineRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "subtotal", expression = "java(entity.getTotalPrice())")
    SupplyOrderLineResponseDTO toResponseDTO(SupplyOrderLine entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<SupplyOrderLineResponseDTO> toResponseDTOList(List<SupplyOrderLine> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "material", ignore = true)
    @Mapping(target = "supplyOrder", ignore = true)
    void updateEntityFromDTO(SupplyOrderLineRequestDTO dto, @MappingTarget SupplyOrderLine entity);
}
