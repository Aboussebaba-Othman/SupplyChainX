package com.supplychainx.common.util;

import com.supplychainx.common.exception.ValidationException;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;


@UtilityClass
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{10,15}$");

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone format");
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    public static void validateNotNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }
    }

    public static void validateRange(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
        double val = value.doubleValue();
        if (val < min.doubleValue() || val > max.doubleValue()) {
            throw new ValidationException(
                String.format("%s must be between %s and %s", fieldName, min, max));
        }
    }
}
