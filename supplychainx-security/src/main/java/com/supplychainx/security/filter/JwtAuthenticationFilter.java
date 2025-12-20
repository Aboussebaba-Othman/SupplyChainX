package com.supplychainx.security.filter;

import com.supplychainx.security.config.JwtProperties;
import com.supplychainx.security.service.JwtTokenService;
import com.supplychainx.security.service.TokenBlacklistService;
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
    private final TokenBlacklistService tokenBlacklistService;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT token from Authorization header
            String token = extractTokenFromRequest(request);

            // Process token if present
            if (token != null) {
                // Check if token is blacklisted (revoked)
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Tentative d'utilisation d'un token blacklisté");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Validate token
                if (jwtTokenService.validateToken(token)) {
                    // Extract username from token
                    String username = jwtTokenService.extractUsername(token);

                    // Load user details
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    // Verify token is valid for this user
                    if (jwtTokenService.validateToken(token, username)) {
                        // Create authentication object
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                        log.debug("Utilisateur authentifié: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT: {}", e.getMessage());
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filter for public endpoints
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
