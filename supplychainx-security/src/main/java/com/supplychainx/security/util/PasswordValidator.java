package com.supplychainx.security.util;

import com.supplychainx.common.exception.ValidationException;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour valider la force des mots de passe
 */
@UtilityClass
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    /**
     * Valide un mot de passe selon les règles de sécurité
     * 
     * @param password Le mot de passe à valider
     * @throws ValidationException si le mot de passe ne respecte pas les règles
     */
    public static void validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Le mot de passe est obligatoire");
        }

        // Longueur minimum
        if (password.length() < MIN_LENGTH) {
            errors.add("Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères");
        }

        // Longueur maximum (pour éviter les attaques DoS)
        if (password.length() > MAX_LENGTH) {
            errors.add("Le mot de passe ne peut pas dépasser " + MAX_LENGTH + " caractères");
        }

        // Au moins une lettre majuscule
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule");
        }

        // Au moins une lettre minuscule
        if (!password.matches(".*[a-z].*")) {
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule");
        }

        // Au moins un chiffre
        if (!password.matches(".*[0-9].*")) {
            errors.add("Le mot de passe doit contenir au moins un chiffre");
        }

        // Au moins un caractère spécial
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            errors.add("Le mot de passe doit contenir au moins un caractère spécial");
        }

        // Mots de passe courants interdits
        if (isCommonPassword(password)) {
            errors.add("Ce mot de passe est trop commun");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation du mot de passe échouée: " + String.join(", ", errors));
        }
    }

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

    public static boolean isStrong(String password) {
        try {
            validate(password);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
