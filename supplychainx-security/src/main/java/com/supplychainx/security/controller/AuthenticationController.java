package com.supplychainx.security.controller;

import com.supplychainx.security.dto.request.LoginRequestDTO;
import com.supplychainx.security.dto.request.RefreshTokenRequestDTO;
import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.AuthenticationResponseDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
import com.supplychainx.security.entity.User;
import com.supplychainx.security.mapper.UserMapper;
import com.supplychainx.security.service.AuthenticationService;
import com.supplychainx.security.service.UserService;
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
    
    /**
     * Authentifie un utilisateur et retourne un token JWT
     * 
     * @param loginRequest les identifiants de connexion
     * @return la réponse d'authentification avec le token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        
        log.info("POST /api/auth/login - Tentative de connexion pour: {}", 
                loginRequest.getUsername());
        
        AuthenticationResponseDTO response = authenticationService.login(loginRequest);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param userRequest les informations de l'utilisateur à créer
     * @return la réponse d'authentification avec le token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDTO> register(
            @Valid @RequestBody UserRequestDTO userRequest) {
        
        log.info("POST /api/auth/register - Tentative d'inscription pour: {}", 
                userRequest.getUsername());
        
        AuthenticationResponseDTO response = authenticationService.register(userRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Rafraîchit le token d'accès à partir d'un refresh token
     * 
     * @param refreshTokenRequest le refresh token
     * @return la nouvelle réponse d'authentification avec les nouveaux tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        
        log.info("POST /api/auth/refresh-token - Tentative de rafraîchissement du token");
        
        AuthenticationResponseDTO response = authenticationService
                .refreshToken(refreshTokenRequest.getRefreshToken());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Récupère les informations de l'utilisateur connecté
     * 
     * @return les informations de l'utilisateur
     */
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
    
    /**
     * Vérifie si un nom d'utilisateur est disponible
     * 
     * @param username le nom d'utilisateur à vérifier
     * @return true si le nom d'utilisateur est disponible
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @RequestParam String username) {
        
        log.info("GET /api/auth/check-username - Vérification de la disponibilité du username: {}", 
                username);
        
        boolean available = !userService.existsByUsername(username);
        
        return ResponseEntity.ok(available);
    }
    
    /**
     * Vérifie si un email est disponible
     * 
     * @param email l'email à vérifier
     * @return true si l'email est disponible
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @RequestParam String email) {
        
        log.info("GET /api/auth/check-email - Vérification de la disponibilité de l'email: {}", 
                email);
        
        boolean available = !userService.existsByEmail(email);
        
        return ResponseEntity.ok(available);
    }
}
