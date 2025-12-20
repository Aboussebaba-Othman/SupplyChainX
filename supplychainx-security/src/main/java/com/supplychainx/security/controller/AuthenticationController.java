package com.supplychainx.security.controller;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.security.constants.SecurityConstants;
import com.supplychainx.security.dto.request.LoginRequestDTO;
import com.supplychainx.security.dto.request.RefreshTokenRequestDTO;
import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.AuthenticationResponseDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
import com.supplychainx.security.entity.User;
import com.supplychainx.security.mapper.UserMapper;
import com.supplychainx.security.service.AuthenticationService;
import com.supplychainx.security.service.JwtTokenService;
import com.supplychainx.security.service.RateLimitingService;
import com.supplychainx.security.service.TokenBlacklistService;
import com.supplychainx.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final RateLimitingService rateLimitingService;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest httpRequest) {

        // Apply rate limiting by client IP
        String clientIp = getClientIp(httpRequest);
        if (!rateLimitingService.tryConsume(clientIp)) {
            log.warn("POST /api/auth/login - Rate limit dépassé pour IP: {}", clientIp);
            throw new BusinessException(SecurityConstants.ERROR_TOO_MANY_LOGIN_ATTEMPTS);
        }

        log.info("POST /api/auth/login - Tentative de connexion pour: {}",
                loginRequest.getUsername());

        AuthenticationResponseDTO response = authenticationService.login(loginRequest);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody UserRequestDTO userRequest) {
        
        log.info("POST /api/auth/register - Tentative d'inscription pour: {}", 
                userRequest.getUsername());
        
        AuthenticationResponseDTO response = authenticationService.register(userRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        
        log.info("POST /api/auth/refresh-token - Tentative de rafraîchissement du token");
        
        AuthenticationResponseDTO response = authenticationService
                .refreshToken(refreshTokenRequest.getRefreshToken());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        log.info("GET /api/auth/me - Récupération des informations de l'utilisateur connecté");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = (User) authentication.getPrincipal();
        UserResponseDTO userResponse = userMapper.toResponseDTO(user);
        
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @RequestParam String username) {
        
        log.info("GET /api/auth/check-username - Vérification de la disponibilité du username: {}", 
                username);
        
        boolean available = !userService.existsByUsername(username);
        
        return ResponseEntity.ok(available);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @RequestParam String email) {

        log.info("GET /api/auth/check-email - Vérification de la disponibilité de l'email: {}",
                email);

        boolean available = !userService.existsByEmail(email);

        return ResponseEntity.ok(available);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("POST /api/auth/logout - Tentative de déconnexion");

        // Extract token from Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.warn("POST /api/auth/logout - Token manquant ou invalide");
            return ResponseEntity.badRequest().build();
        }

        String token = jwtTokenService.extractTokenFromBearer(bearerToken);

        // Add token to blacklist with remaining expiration duration
        try {
            var remainingDuration = jwtTokenService.getRemainingExpiration(token);
            tokenBlacklistService.blacklist(token, remainingDuration);
            log.info("POST /api/auth/logout - Token révoqué avec succès");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("POST /api/auth/logout - Erreur lors de la révocation du token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get client IP address (considers proxy headers)
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
