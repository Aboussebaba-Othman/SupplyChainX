package com.supplychainx.security.service;

import com.supplychainx.security.dto.request.LoginRequestDTO;
import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.AuthenticationResponseDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
import com.supplychainx.security.entity.RefreshToken;
import com.supplychainx.security.entity.User;
import com.supplychainx.security.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    

    @Transactional
    public AuthenticationResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Tentative de connexion pour l'utilisateur: {}", loginRequest.getUsername());
        
        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Récupérer l'utilisateur authentifié
            User user = (User) authentication.getPrincipal();
            
            // Update last login date
            userService.updateLastLogin(user.getUsername());

            // Generate access token
            String accessToken = jwtTokenService.generateToken(user);

            // Create and save refresh token in database
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("Connexion réussie pour l'utilisateur: {}", user.getUsername());

            // Build response
            UserResponseDTO userResponse = userMapper.toResponseDTO(user);

            return AuthenticationResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .expiresIn(86400000L) // 24 hours
                    .user(userResponse)
                    .build();
                    
        } catch (BadCredentialsException e) {
            log.warn("Échec de connexion pour l'utilisateur: {}", loginRequest.getUsername());
            
            // Gérer les tentatives de connexion échouées
            try {
                userService.handleFailedLogin(loginRequest.getUsername());
            } catch (UsernameNotFoundException ignored) {
                // L'utilisateur n'existe pas, on ne fait rien
            }
            
            throw new BadCredentialsException("Nom d'utilisateur ou mot de passe incorrect");
        }
    }
    
    // Register new user and generate tokens
    @Transactional
    public AuthenticationResponseDTO register(UserRequestDTO userRequest) {
        log.info("Tentative d'inscription pour l'utilisateur: {}", userRequest.getUsername());

        // Create user
        UserResponseDTO createdUser = userService.createUser(userRequest);

        // Get User entity to generate tokens
        User user = userService.getUserEntityByUsername(createdUser.getUsername());

        // Generate access token
        String accessToken = jwtTokenService.generateToken(user);

        // Create and save refresh token in database
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Inscription réussie pour l'utilisateur: {}", user.getUsername());

        // Build response
        return AuthenticationResponseDTO.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(86400000L) // 24 hours
                .user(createdUser)
                .build();
    }
    

    // Refresh access token using refresh token (with rotation)
    @Transactional
    public AuthenticationResponseDTO refreshToken(String refreshTokenString) {
        log.debug("Tentative de rafraîchissement du token");

        // Find and verify refresh token from database
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        // Get user from refresh token
        User user = refreshToken.getUser();

        // Verify account is active
        if (!user.isEnabled()) {
            log.warn("Tentative de rafraîchissement du token pour un compte désactivé: {}", user.getUsername());
            throw new BadCredentialsException("Compte utilisateur désactivé");
        }

        if (!user.isAccountNonLocked()) {
            log.warn("Tentative de rafraîchissement du token pour un compte verrouillé: {}", user.getUsername());
            throw new BadCredentialsException("Compte utilisateur verrouillé");
        }

        // Update last used timestamp
        refreshTokenService.updateLastUsed(refreshToken);

        // Rotate refresh token (revoke old, create new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        // Generate new access token
        String newAccessToken = jwtTokenService.generateToken(user);

        log.info("Token rafraîchi avec succès pour l'utilisateur: {}", user.getUsername());

        // Build response
        UserResponseDTO userResponse = userMapper.toResponseDTO(user);

        return AuthenticationResponseDTO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(86400000L) // 24 hours
                .user(userResponse)
                .build();
    }
}
