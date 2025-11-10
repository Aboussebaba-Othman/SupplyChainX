package com.supplychainx.audit.mapper;

import com.supplychainx.audit.dto.request.AuditLogRequestDTO;
import com.supplychainx.audit.dto.response.AuditLogResponseDTO;
import com.supplychainx.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper pour convertir entre entités AuditLog et DTOs
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    AuditLog toEntity(AuditLogRequestDTO dto);

    @Mapping(target = "createdAt", source = "createdAt")
    AuditLogResponseDTO toResponseDTO(AuditLog entity);

    List<AuditLogResponseDTO> toResponseDTOList(List<AuditLog> entities);
}
