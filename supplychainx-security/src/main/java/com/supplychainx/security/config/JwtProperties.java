package com.supplychainx.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//Configuration des propriétés JWT

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * Clé secrète pour signer les tokens JWT (min 256 bits)
     * À définir dans application.yml
     */
    private String secret;
    
    /**
     * Durée de validité du token d'accès en millisecondes
     * Par défaut: 24 heures (86400000 ms)
     */
    private Long expiration = 86400000L; // 24 heures
    
    /**
     * Durée de validité du refresh token en millisecondes
     * Par défaut: 7 jours (604800000 ms)
     */
    private Long refreshExpiration = 604800000L; // 7 jours
    
    /**
     * Préfixe du token dans le header Authorization
     * Par défaut: "Bearer "
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * Nom du header contenant le token JWT
     * Par défaut: "Authorization"
     */
    private String headerString = "Authorization";
}
