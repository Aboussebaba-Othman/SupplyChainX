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

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        
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
}
