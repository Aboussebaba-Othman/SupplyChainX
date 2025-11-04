package com.supplychainx.security.mapper;

import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
import com.supplychainx.security.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert User entity to UserResponseDTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roleDescription", expression = "java(user.getRole().getDescription())")
    UserResponseDTO toResponseDTO(User user);

    /**
     * Convert UserRequestDTO to User entity (for creation)
     * Password will be encrypted in the service layer
     */
    User toEntity(UserRequestDTO dto);
}
