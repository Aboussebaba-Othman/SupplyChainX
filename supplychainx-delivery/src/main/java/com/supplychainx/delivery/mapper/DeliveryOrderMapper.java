package com.supplychainx.delivery.mapper;

import com.supplychainx.delivery.dto.request.DeliveryOrderRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryOrderResponseDTO;
import com.supplychainx.delivery.entity.DeliveryOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CustomerMapper.class, DeliveryOrderLineMapper.class})
public interface DeliveryOrderMapper {

    // Convertir RequestDTO vers Entity (customer et orderLines gérés manuellement dans le service)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "orderLines", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    DeliveryOrder toEntity(DeliveryOrderRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    @Mapping(target = "totalAmount", expression = "java(entity.calculateTotalAmount())")
    DeliveryOrderResponseDTO toResponseDTO(DeliveryOrder entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<DeliveryOrderResponseDTO> toResponseDTOList(List<DeliveryOrder> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "orderLines", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    void updateEntityFromDTO(DeliveryOrderRequestDTO dto, @MappingTarget DeliveryOrder entity);
}
