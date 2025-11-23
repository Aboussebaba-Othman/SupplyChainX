package com.supplychainx.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//Configuration des propriétés JWT

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private Long expiration = 86400000L; // 24 heures
    private Long refreshExpiration = 604800000L; // 7 jours
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
}
