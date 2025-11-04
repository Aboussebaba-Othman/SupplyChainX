package com.supplychainx.production.mapper;

import com.supplychainx.production.dto.request.ProductionOrderRequestDTO;
import com.supplychainx.production.dto.response.ProductionOrderResponseDTO;
import com.supplychainx.production.entity.ProductionOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProductMapper.class})
public interface ProductionOrderMapper {

    // Convertir RequestDTO vers Entity (product géré manuellement dans le service)
    @Mapping(target = "product", ignore = true)
    ProductionOrder toEntity(ProductionOrderRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "delayed", expression = "java(entity.isDelayed())")
    ProductionOrderResponseDTO toResponseDTO(ProductionOrder entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<ProductionOrderResponseDTO> toResponseDTOList(List<ProductionOrder> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromDTO(ProductionOrderRequestDTO dto, @MappingTarget ProductionOrder entity);
}
