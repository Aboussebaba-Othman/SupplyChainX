package com.supplychainx.supply.mapper;

import com.supplychainx.supply.dto.request.SupplyOrderRequestDTO;
import com.supplychainx.supply.dto.response.SupplyOrderResponseDTO;
import com.supplychainx.supply.entity.SupplyOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {SupplierMapper.class, SupplyOrderLineMapper.class})
public interface SupplyOrderMapper {

    // Convertir RequestDTO vers Entity (supplier et orderLines gérés manuellement dans le service)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "orderLines", ignore = true)
    SupplyOrder toEntity(SupplyOrderRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "totalAmount", expression = "java(entity.getTotalAmount())")
    SupplyOrderResponseDTO toResponseDTO(SupplyOrder entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<SupplyOrderResponseDTO> toResponseDTOList(List<SupplyOrder> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "orderLines", ignore = true)
    void updateEntityFromDTO(SupplyOrderRequestDTO dto, @MappingTarget SupplyOrder entity);
}
