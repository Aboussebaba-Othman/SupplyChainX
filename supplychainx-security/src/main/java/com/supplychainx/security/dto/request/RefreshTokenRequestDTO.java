package com.supplychainx.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le rafraîchissement du token
 * 
 * @author SupplyChainX Team
 * @version 1.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}
