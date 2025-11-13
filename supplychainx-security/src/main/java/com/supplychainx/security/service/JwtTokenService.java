package com.supplychainx.security.service;

import com.supplychainx.security.config.JwtProperties;
import com.supplychainx.security.entity.User;
import com.supplychainx.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {
    
    private final JwtProperties jwtProperties;
    
    /**
     * Génère un token d'accès JWT pour l'utilisateur
     * 
     * @param user l'utilisateur pour lequel générer le token
     * @return le token JWT signé
     */
    public String generateToken(User user) {
        log.debug("Génération du token d'accès pour l'utilisateur: {}", user.getUsername());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        claims.put("userId", user.getId());
        
        return createToken(claims, user.getUsername(), jwtProperties.getExpiration());
    }
    
    /**
     * Génère un refresh token pour l'utilisateur
     * 
     * @param user l'utilisateur pour lequel générer le refresh token
     * @return le refresh token JWT signé
     */
    public String generateRefreshToken(User user) {
        log.debug("Génération du refresh token pour l'utilisateur: {}", user.getUsername());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");
        
        return createToken(claims, user.getUsername(), jwtProperties.getRefreshExpiration());
    }
    
    /**
     * Crée un token JWT avec les claims, le sujet et la durée de validité spécifiés
     * 
     * @param claims les claims à inclure dans le token
     * @param subject le sujet du token (username)
     * @param expiration la durée de validité en millisecondes
     * @return le token JWT signé
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Valide un token JWT
     * 
     * @param token le token à valider
     * @param username le nom d'utilisateur attendu
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            boolean isValid = tokenUsername.equals(username) && !isTokenExpired(token);
            
            if (isValid) {
                log.debug("Token valide pour l'utilisateur: {}", username);
            } else {
                log.warn("Token invalide pour l'utilisateur: {}", username);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Erreur lors de la validation du token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valide un token JWT (sans vérifier l'username)
     * 
     * @param token le token à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            boolean isValid = !isTokenExpired(token);
            
            if (isValid) {
                log.debug("Token valide");
            } else {
                log.warn("Token expiré");
            }
            
            return isValid;
        } catch (SignatureException e) {
            log.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformé: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Claims JWT vides: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Extrait le nom d'utilisateur du token JWT
     * 
     * @param token le token JWT
     * @return le nom d'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrait le rôle du token JWT
     * 
     * @param token le token JWT
     * @return le rôle de l'utilisateur
     */
    public Role extractRole(String token) {
        Claims claims = extractAllClaims(token);
        String roleName = claims.get("role", String.class);
        return Role.valueOf(roleName);
    }
    
    /**
     * Extrait l'ID utilisateur du token JWT
     * 
     * @param token le token JWT
     * @return l'ID de l'utilisateur
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extrait la date d'expiration du token JWT
     * 
     * @param token le token JWT
     * @return la date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrait un claim spécifique du token JWT
     * 
     * @param token le token JWT
     * @param claimsResolver la fonction pour extraire le claim
     * @param <T> le type du claim
     * @return la valeur du claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extrait tous les claims du token JWT
     * 
     * @param token le token JWT
     * @return les claims du token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Vérifie si le token JWT est expiré
     * 
     * @param token le token JWT
     * @return true si le token est expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Obtient la clé de signature pour les tokens JWT
     * 
     * @return la clé de signature
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Extrait le token JWT du header Authorization
     * 
     * @param bearerToken le header Authorization complet
     * @return le token JWT sans le préfixe "Bearer "
     */
    public String extractTokenFromBearer(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }
}
