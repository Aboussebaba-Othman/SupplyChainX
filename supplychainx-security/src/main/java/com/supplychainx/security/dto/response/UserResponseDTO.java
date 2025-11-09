package com.supplychainx.security.dto.response;

import com.supplychainx.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private String roleDescription;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private String createdBy;
}
