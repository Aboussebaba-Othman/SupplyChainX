package com.supplychainx.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponseDTO {

    private String token;
    private String refreshToken;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Long expiresIn; // in seconds
    private UserResponseDTO user;
}
