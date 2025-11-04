package com.supplychainx.security.service;

import com.supplychainx.security.dto.request.LoginRequestDTO;
import com.supplychainx.security.dto.request.UserRequestDTO;
import com.supplychainx.security.dto.response.AuthenticationResponseDTO;
import com.supplychainx.security.dto.response.UserResponseDTO;
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
    
    /**
     * Authentifie un utilisateur et génère les tokens JWT
     * 
     * @param loginRequest les identifiants de connexion
     * @return la réponse d'authentification avec les tokens
     * @throws BadCredentialsException si les identifiants sont invalides
     */
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
            
            // Mettre à jour la date de dernière connexion
            userService.updateLastLogin(user.getUsername());
            
            // Générer les tokens
            String accessToken = jwtTokenService.generateToken(user);
            String refreshToken = jwtTokenService.generateRefreshToken(user);
            
            log.info("Connexion réussie pour l'utilisateur: {}", user.getUsername());
            
            // Construire la réponse
            UserResponseDTO userResponse = userMapper.toResponseDTO(user);
            
            return AuthenticationResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(86400000L) // 24 heures
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
    
    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param userRequest les informations de l'utilisateur à créer
     * @return la réponse d'authentification avec les tokens
     */
    @Transactional
    public AuthenticationResponseDTO register(UserRequestDTO userRequest) {
        log.info("Tentative d'inscription pour l'utilisateur: {}", userRequest.getUsername());
        
        // Créer l'utilisateur
        UserResponseDTO createdUser = userService.createUser(userRequest);
        
        // Récupérer l'entité User pour générer les tokens
        User user = userService.getUserEntityByUsername(createdUser.getUsername());
        
        // Générer les tokens
        String accessToken = jwtTokenService.generateToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        
        log.info("Inscription réussie pour l'utilisateur: {}", user.getUsername());
        
        // Construire la réponse
        return AuthenticationResponseDTO.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L) // 24 heures
                .user(createdUser)
                .build();
    }
    
    /**
     * Rafraîchit le token d'accès à partir d'un refresh token
     * 
     * @param refreshToken le refresh token
     * @return la nouvelle réponse d'authentification avec les nouveaux tokens
     * @throws BadCredentialsException si le refresh token est invalide
     */
    @Transactional(readOnly = true)
    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        log.debug("Tentative de rafraîchissement du token");
        
        // Valider le refresh token
        if (!jwtTokenService.validateToken(refreshToken)) {
            log.warn("Refresh token invalide");
            throw new BadCredentialsException("Refresh token invalide ou expiré");
        }
        
        // Extraire le nom d'utilisateur du refresh token
        String username = jwtTokenService.extractUsername(refreshToken);
        
        // Récupérer l'utilisateur
        User user = userService.getUserEntityByUsername(username);
        
        // Vérifier que le compte est actif
        if (!user.isEnabled()) {
            log.warn("Tentative de rafraîchissement du token pour un compte désactivé: {}", username);
            throw new BadCredentialsException("Compte utilisateur désactivé");
        }
        
        if (!user.isAccountNonLocked()) {
            log.warn("Tentative de rafraîchissement du token pour un compte verrouillé: {}", username);
            throw new BadCredentialsException("Compte utilisateur verrouillé");
        }
        
        // Générer de nouveaux tokens
        String newAccessToken = jwtTokenService.generateToken(user);
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);
        
        log.info("Token rafraîchi avec succès pour l'utilisateur: {}", username);
        
        // Construire la réponse
        UserResponseDTO userResponse = userMapper.toResponseDTO(user);
        
        return AuthenticationResponseDTO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400000L) // 24 heures
                .user(userResponse)
                .build();
    }
}
