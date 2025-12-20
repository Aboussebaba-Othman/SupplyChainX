package com.supplychainx.security.constants;

// Security constants to replace magic numbers and improve maintainability
public final class SecurityConstants {

    // Prevent instantiation
    private SecurityConstants() {
        throw new UnsupportedOperationException("Cette classe ne peut pas être instanciée");
    }

    // ==================== Password Constants ====================

    // Minimum password length
    public static final int PASSWORD_MIN_LENGTH = 8;

    // Maximum password length
    public static final int PASSWORD_MAX_LENGTH = 128;

    // BCrypt encoding strength (rounds: 4-31, recommended: 12-14)
    public static final int BCRYPT_STRENGTH = 12;

    // ==================== Rate Limiting Constants ====================

    // Maximum login attempts per minute
    public static final int MAX_LOGIN_ATTEMPTS_PER_MINUTE = 5;

    // Lockout duration after exceeding attempts (in minutes)
    public static final int RATE_LIMIT_REFILL_DURATION_MINUTES = 1;

    // ==================== Validation Constants ====================

    // Email validation regex pattern
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Username validation regex (alphanumeric with underscore/dash, 3-50 chars)
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,50}$";

    // Minimum username length
    public static final int USERNAME_MIN_LENGTH = 3;

    // Maximum username length
    public static final int USERNAME_MAX_LENGTH = 50;

    // ==================== JWT Token Constants ====================

    // Access token validity duration (24 hours = 86400000 ms)
    public static final long JWT_ACCESS_TOKEN_EXPIRATION_MS = 86400000L;

    // Refresh token validity duration (7 days = 604800000 ms)
    public static final long JWT_REFRESH_TOKEN_EXPIRATION_MS = 604800000L;

    // ==================== Session Constants ====================

    // Maximum active sessions per user
    public static final int MAX_ACTIVE_SESSIONS_PER_USER = 5;

    // Session inactivity timeout (in minutes)
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    // ==================== CORS Constants ====================

    // CORS configuration cache duration (1 hour = 3600 seconds)
    public static final long CORS_MAX_AGE_SECONDS = 3600L;

    // ==================== Error Messages ====================

    // Too many login attempts error
    public static final String ERROR_TOO_MANY_LOGIN_ATTEMPTS =
        "Trop de tentatives de connexion. Veuillez réessayer dans 1 minute.";

    // Invalid or expired token error
    public static final String ERROR_INVALID_TOKEN =
        "Token invalide ou expiré. Veuillez vous reconnecter.";

    // Blacklisted token error
    public static final String ERROR_TOKEN_BLACKLISTED =
        "Ce token a été révoqué. Veuillez vous reconnecter.";

    // Invalid credentials error
    public static final String ERROR_INVALID_CREDENTIALS =
        "Nom d'utilisateur ou mot de passe invalide.";

    // Username already exists error
    public static final String ERROR_USERNAME_ALREADY_EXISTS =
        "Ce nom d'utilisateur est déjà utilisé.";

    // Email already exists error
    public static final String ERROR_EMAIL_ALREADY_EXISTS =
        "Cet email est déjà utilisé.";
}
