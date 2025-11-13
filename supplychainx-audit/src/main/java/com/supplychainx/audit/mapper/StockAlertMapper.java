package com.supplychainx.audit.mapper;

import com.supplychainx.audit.dto.request.StockAlertRequestDTO;
import com.supplychainx.audit.dto.response.StockAlertResponseDTO;
import com.supplychainx.audit.entity.StockAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper pour convertir entre entités StockAlert et DTOs
 */
@Mapper(componentModel = "spring")
public interface StockAlertMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    StockAlert toEntity(StockAlertRequestDTO dto);

    @Mapping(target = "critical", expression = "java(entity.isCritical())")
    StockAlertResponseDTO toResponseDTO(StockAlert entity);

    List<StockAlertResponseDTO> toResponseDTOList(List<StockAlert> entities);
}
