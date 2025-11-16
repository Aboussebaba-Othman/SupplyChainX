package com.supplychainx.common.util;

import com.supplychainx.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil Unit Tests")
class ValidationUtilTest {

    // ========== Email Validation Tests ==========
    
    @Test
    @DisplayName("Should validate correct email")
    void shouldValidateCorrectEmail() {
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user.name@domain.co.uk"));
        assertDoesNotThrow(() -> ValidationUtil.validateEmail("user+tag@example.org"));
    }

    @Test
    @DisplayName("Should throw exception for null email")
    void shouldThrowExceptionForNullEmail() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateEmail(null));
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank email")
    void shouldThrowExceptionForBlankEmail() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateEmail("   "));
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid email format")
    void shouldThrowExceptionForInvalidEmailFormat() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateEmail("invalid-email"));
        assertEquals("Invalid email format", exception.getMessage());
        
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("@example.com"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("user@"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validateEmail("user@domain"));
    }

    // ========== Phone Validation Tests ==========
    
    @Test
    @DisplayName("Should validate correct phone numbers")
    void shouldValidateCorrectPhoneNumbers() {
        assertDoesNotThrow(() -> ValidationUtil.validatePhone("+1234567890"));
        assertDoesNotThrow(() -> ValidationUtil.validatePhone("1234567890"));
        assertDoesNotThrow(() -> ValidationUtil.validatePhone("+212600123456"));
    }

    @Test
    @DisplayName("Should accept null or blank phone")
    void shouldAcceptNullOrBlankPhone() {
        assertDoesNotThrow(() -> ValidationUtil.validatePhone(null));
        assertDoesNotThrow(() -> ValidationUtil.validatePhone(""));
        assertDoesNotThrow(() -> ValidationUtil.validatePhone("   "));
    }

    @Test
    @DisplayName("Should throw exception for invalid phone format")
    void shouldThrowExceptionForInvalidPhoneFormat() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validatePhone("123"));
        assertEquals("Invalid phone format", exception.getMessage());
        
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("abc1234567890"));
        assertThrows(ValidationException.class, () -> ValidationUtil.validatePhone("123-456-7890"));
    }

    // ========== Not Blank Validation Tests ==========
    
    @Test
    @DisplayName("Should validate not blank string")
    void shouldValidateNotBlankString() {
        assertDoesNotThrow(() -> ValidationUtil.validateNotBlank("test", "Field"));
        assertDoesNotThrow(() -> ValidationUtil.validateNotBlank("value", "Name"));
    }

    @Test
    @DisplayName("Should throw exception for null value")
    void shouldThrowExceptionForNullValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateNotBlank(null, "Username"));
        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for blank value")
    void shouldThrowExceptionForBlankValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateNotBlank("   ", "Name"));
        assertEquals("Name cannot be empty", exception.getMessage());
    }

    // ========== Positive Validation Tests ==========
    
    @Test
    @DisplayName("Should validate positive numbers")
    void shouldValidatePositiveNumbers() {
        assertDoesNotThrow(() -> ValidationUtil.validatePositive(10, "Quantity"));
        assertDoesNotThrow(() -> ValidationUtil.validatePositive(0.1, "Price"));
        assertDoesNotThrow(() -> ValidationUtil.validatePositive(100L, "Stock"));
    }

    @Test
    @DisplayName("Should throw exception for null positive value")
    void shouldThrowExceptionForNullPositiveValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validatePositive(null, "Price"));
        assertEquals("Price must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for zero or negative value")
    void shouldThrowExceptionForZeroOrNegativeValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validatePositive(0, "Quantity"));
        assertEquals("Quantity must be positive", exception.getMessage());
        
        exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validatePositive(-5, "Stock"));
        assertEquals("Stock must be positive", exception.getMessage());
    }

    // ========== Not Negative Validation Tests ==========
    
    @Test
    @DisplayName("Should validate not negative numbers")
    void shouldValidateNotNegativeNumbers() {
        assertDoesNotThrow(() -> ValidationUtil.validateNotNegative(0, "Balance"));
        assertDoesNotThrow(() -> ValidationUtil.validateNotNegative(10, "Quantity"));
        assertDoesNotThrow(() -> ValidationUtil.validateNotNegative(100.5, "Amount"));
    }

    @Test
    @DisplayName("Should throw exception for null not negative value")
    void shouldThrowExceptionForNullNotNegativeValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateNotNegative(null, "Balance"));
        assertEquals("Balance cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for negative value")
    void shouldThrowExceptionForNegativeValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateNotNegative(-1, "Stock"));
        assertEquals("Stock cannot be negative", exception.getMessage());
    }

    // ========== Range Validation Tests ==========
    
    @Test
    @DisplayName("Should validate value in range")
    void shouldValidateValueInRange() {
        assertDoesNotThrow(() -> ValidationUtil.validateRange(50, 0, 100, "Percentage"));
        assertDoesNotThrow(() -> ValidationUtil.validateRange(0, 0, 100, "Min"));
        assertDoesNotThrow(() -> ValidationUtil.validateRange(100, 0, 100, "Max"));
        assertDoesNotThrow(() -> ValidationUtil.validateRange(5.5, 1.0, 10.0, "Rating"));
    }

    @Test
    @DisplayName("Should throw exception for null range value")
    void shouldThrowExceptionForNullRangeValue() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateRange(null, 0, 100, "Score"));
        assertEquals("Score cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for value below minimum")
    void shouldThrowExceptionForValueBelowMinimum() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateRange(-1, 0, 100, "Age"));
        assertTrue(exception.getMessage().contains("must be between"));
        assertTrue(exception.getMessage().contains("Age"));
    }

    @Test
    @DisplayName("Should throw exception for value above maximum")
    void shouldThrowExceptionForValueAboveMaximum() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> ValidationUtil.validateRange(101, 0, 100, "Percentage"));
        assertTrue(exception.getMessage().contains("must be between"));
        assertTrue(exception.getMessage().contains("Percentage"));
    }
}
