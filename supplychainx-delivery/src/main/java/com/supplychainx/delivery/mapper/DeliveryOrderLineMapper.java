package com.supplychainx.delivery.mapper;

import com.supplychainx.delivery.dto.request.DeliveryOrderLineRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryOrderLineResponseDTO;
import com.supplychainx.delivery.entity.DeliveryOrderLine;
import com.supplychainx.production.mapper.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProductMapper.class})
public interface DeliveryOrderLineMapper {

    // Convertir RequestDTO vers Entity (product géré manuellement dans le service)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "deliveryOrder", ignore = true)
    DeliveryOrderLine toEntity(DeliveryOrderLineRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "lineTotal", expression = "java(entity.getLineTotal())")
    DeliveryOrderLineResponseDTO toResponseDTO(DeliveryOrderLine entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<DeliveryOrderLineResponseDTO> toResponseDTOList(List<DeliveryOrderLine> entities);
}
