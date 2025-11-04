package com.supplychainx.delivery.mapper;

import com.supplychainx.delivery.dto.request.DeliveryRequestDTO;
import com.supplychainx.delivery.dto.response.DeliveryResponseDTO;
import com.supplychainx.delivery.entity.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {DeliveryOrderMapper.class})
public interface DeliveryMapper {

    // Convertir RequestDTO vers Entity (deliveryOrder géré manuellement dans le service)
    @Mapping(target = "deliveryOrder", ignore = true)
    Delivery toEntity(DeliveryRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    DeliveryResponseDTO toResponseDTO(Delivery entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<DeliveryResponseDTO> toResponseDTOList(List<Delivery> entities);

    // Mettre à jour une entité existante avec les données du DTO
    @Mapping(target = "deliveryOrder", ignore = true)
    void updateEntityFromDTO(DeliveryRequestDTO dto, @MappingTarget Delivery entity);
}
