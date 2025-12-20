package com.supplychainx.security.service;

import com.supplychainx.security.constants.SecurityConstants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Rate limiting service using Token Bucket algorithm (Bucket4j)
@Service
@Slf4j
public class RateLimitingService {

    // In-memory cache for buckets (use Redis in production for horizontal scaling)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    // Security configuration constants
    private static final int MAX_ATTEMPTS = SecurityConstants.MAX_LOGIN_ATTEMPTS_PER_MINUTE;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(
        SecurityConstants.RATE_LIMIT_REFILL_DURATION_MINUTES
    );

    // Resolve or create bucket for given key (IP or username)
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> {
            log.debug("Création d'un nouveau bucket pour la clé: {}", k);
            return createNewBucket();
        });
    }

    // Create new bucket with configured limits (5 tokens, refill every minute)
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(MAX_ATTEMPTS)
            .refillIntervally(MAX_ATTEMPTS, REFILL_DURATION)
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    // Try to consume 1 token from bucket (returns false if limit reached)
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit dépassé pour la clé: {}", key);
        }

        return consumed;
    }

    // Get available tokens without consuming
    public long getAvailableTokens(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.getAvailableTokens();
    }

    // Clear cache (for tests or maintenance)
    public void clearCache() {
        cache.clear();
        log.info("Cache de rate limiting vidé");
    }
}
