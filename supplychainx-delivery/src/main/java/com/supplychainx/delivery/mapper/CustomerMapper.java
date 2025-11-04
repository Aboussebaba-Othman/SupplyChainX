package com.supplychainx.delivery.mapper;

import com.supplychainx.delivery.dto.request.CustomerRequestDTO;
import com.supplychainx.delivery.dto.response.CustomerResponseDTO;
import com.supplychainx.delivery.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    // Convertir RequestDTO vers Entity
    Customer toEntity(CustomerRequestDTO dto);

    // Convertir Entity vers ResponseDTO
    CustomerResponseDTO toResponseDTO(Customer entity);

    // Convertir une liste d'entités vers une liste de DTOs
    List<CustomerResponseDTO> toResponseDTOList(List<Customer> entities);

    // Mettre à jour une entité existante avec les données du DTO
    void updateEntityFromDTO(CustomerRequestDTO dto, @MappingTarget Customer entity);
}
