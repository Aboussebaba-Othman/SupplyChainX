package com.supplychainx.security.util;

import com.supplychainx.common.exception.ValidationException;
import com.supplychainx.security.constants.SecurityConstants;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

// Password strength validator using SecurityConstants
@UtilityClass
public class PasswordValidator {

    private static final int MIN_LENGTH = SecurityConstants.PASSWORD_MIN_LENGTH;
    private static final int MAX_LENGTH = SecurityConstants.PASSWORD_MAX_LENGTH;

    // Validate password against security rules
    public static void validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Le mot de passe est obligatoire");
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            errors.add("Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères");
        }

        // Check maximum length (prevents DoS attacks)
        if (password.length() > MAX_LENGTH) {
            errors.add("Le mot de passe ne peut pas dépasser " + MAX_LENGTH + " caractères");
        }

        // Require at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule");
        }

        // Require at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule");
        }

        // Require at least one digit
        if (!password.matches(".*[0-9].*")) {
            errors.add("Le mot de passe doit contenir au moins un chiffre");
        }

        // Require at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("Le mot de passe doit contenir au moins un caractère spécial");
        }

        // Reject common passwords
        if (isCommonPassword(password)) {
            errors.add("Ce mot de passe est trop commun");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation du mot de passe échouée: " + String.join(", ", errors));
        }
    }

    // Check if password contains common weak patterns
    private static boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
            "password", "123456", "12345678", "qwerty", "abc123",
            "admin", "password123", "welcome", "login"
        };
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    // Check if password is strong (returns boolean)
    public static boolean isStrong(String password) {
        try {
            validate(password);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
