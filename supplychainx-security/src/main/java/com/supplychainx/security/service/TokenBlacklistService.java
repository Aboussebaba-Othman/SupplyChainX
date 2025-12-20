package com.supplychainx.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

// JWT token blacklist service for logout functionality (use Redis in production)
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    // In-memory blacklist (use Redis in production for distributed storage & persistence)
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // Add token to blacklist with auto-removal after expiration
    public void blacklist(String token, Duration duration) {
        blacklistedTokens.add(token);
        log.info("Token ajouté à la blacklist (expire dans {} minutes)", duration.toMinutes());

        // Schedule automatic removal after expiration (prevents memory leaks)
        CompletableFuture.delayedExecutor(duration.toMillis(), TimeUnit.MILLISECONDS)
                .execute(() -> {
                    blacklistedTokens.remove(token);
                    log.debug("Token retiré de la blacklist après expiration");
                });
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // Remove token from blacklist (mainly for tests)
    public void removeFromBlacklist(String token) {
        boolean removed = blacklistedTokens.remove(token);
        if (removed) {
            log.debug("Token retiré manuellement de la blacklist");
        }
    }

    // Clear entire blacklist (for tests or maintenance)
    public void clearBlacklist() {
        int size = blacklistedTokens.size();
        blacklistedTokens.clear();
        log.info("{} tokens retirés de la blacklist", size);
    }

    // Get current blacklist size
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}
