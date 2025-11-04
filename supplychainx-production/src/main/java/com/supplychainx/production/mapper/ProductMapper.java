package com.supplychainx.production.mapper;

import com.supplychainx.production.dto.request.ProductRequestDTO;
import com.supplychainx.production.dto.response.ProductResponseDTO;
import com.supplychainx.production.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // Convertir RequestDTO vers Entity
    Product toEntity(ProductRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "lowStock", expression = "java(entity.isLowStock())")
    ProductResponseDTO toResponseDTO(Product entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<ProductResponseDTO> toResponseDTOList(List<Product> entities);

    // Mettre à jour une entité existante avec les données du DTO
    void updateEntityFromDTO(ProductRequestDTO dto, @MappingTarget Product entity);
}
