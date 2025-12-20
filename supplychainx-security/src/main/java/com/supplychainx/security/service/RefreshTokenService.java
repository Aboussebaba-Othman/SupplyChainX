package com.supplychainx.security.service;

import com.supplychainx.common.exception.BusinessException;
import com.supplychainx.security.config.JwtProperties;
import com.supplychainx.security.entity.RefreshToken;
import com.supplychainx.security.entity.User;
import com.supplychainx.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// Service for managing refresh tokens (storage, validation, rotation, revocation)
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    // Create and save a new refresh token for a user
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Calculate expiration date (7 days from now)
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshExpiration() / 1000);

        // Generate unique token string
        String tokenString = UUID.randomUUID().toString();

        // Create and save refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenString)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user: {}", user.getUsername());

        return refreshToken;
    }

    // Find refresh token by token string
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token not found"));
    }

    // Verify and return valid refresh token
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token expired. Please login again.");
        }

        if (token.isRevoked()) {
            throw new BusinessException("Refresh token has been revoked. Please login again.");
        }

        return token;
    }

    // Rotate refresh token (revoke old, create new)
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        log.info("Old refresh token revoked for user: {}", oldToken.getUser().getUsername());

        // Create new token
        RefreshToken newToken = createRefreshToken(oldToken.getUser());
        log.info("New refresh token created for user: {}", oldToken.getUser().getUsername());

        return newToken;
    }

    // Revoke a specific refresh token
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token revoked for user: {}", refreshToken.getUser().getUsername());
    }

    // Revoke all tokens for a user (logout from all devices)
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("All refresh tokens revoked for user: {}", user.getUsername());
    }

    // Update last used timestamp
    @Transactional
    public void updateLastUsed(RefreshToken token) {
        token.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
    }

    // Cleanup expired tokens (scheduled job)
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired refresh tokens deleted");
    }

    // Cleanup old revoked tokens (scheduled job)
    @Transactional
    public void deleteOldRevokedTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        refreshTokenRepository.deleteOldRevokedTokens(cutoffDate);
        log.info("Old revoked tokens (older than {} days) deleted", daysOld);
    }
}
