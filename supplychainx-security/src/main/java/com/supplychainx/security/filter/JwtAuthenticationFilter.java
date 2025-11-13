package com.supplychainx.security.filter;

import com.supplychainx.security.config.JwtProperties;
import com.supplychainx.security.service.JwtTokenService;
import com.supplychainx.security.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final JwtProperties jwtProperties;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extraire le token JWT du header Authorization
            String token = extractTokenFromRequest(request);
            
            // Si le token existe et est valide
            if (token != null && jwtTokenService.validateToken(token)) {
                // Extraire le nom d'utilisateur du token
                String username = jwtTokenService.extractUsername(token);
                
                // Charger les détails de l'utilisateur
                UserDetails userDetails = userService.loadUserByUsername(username);
                
                // Vérifier que le token est valide pour cet utilisateur
                if (jwtTokenService.validateToken(token, username)) {
                    // Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    log.debug("Utilisateur authentifié: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT: {}", e.getMessage());
        }
        
        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Ne pas appliquer le filtre sur les endpoints publics
        return path.startsWith("/api/auth/") ||
               path.startsWith("/test/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.equals("/swagger-ui.html") ||
               path.equals("/actuator/health");
    }
  
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeaderString());
        
        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        
        return null;
    }
}
