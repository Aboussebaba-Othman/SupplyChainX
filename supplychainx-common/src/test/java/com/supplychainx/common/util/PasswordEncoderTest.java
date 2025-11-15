package com.supplychainx.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordEncoder Unit Tests")
class PasswordEncoderTest {

    @Test
    @DisplayName("Should encode password")
    void shouldEncodePassword() {
        String rawPassword = "mySecurePassword123";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$")); // BCrypt prefix
    }

    @Test
    @DisplayName("Should produce different encodings for same password")
    void shouldProduceDifferentEncodingsForSamePassword() {
        String rawPassword = "password123";
        String encoded1 = PasswordEncoder.encode(rawPassword);
        String encoded2 = PasswordEncoder.encode(rawPassword);
        
        assertNotEquals(encoded1, encoded2); // BCrypt uses random salt
    }

    @Test
    @DisplayName("Should match correct password")
    void shouldMatchCorrectPassword() {
        String rawPassword = "myPassword123";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertTrue(PasswordEncoder.matches(rawPassword, encoded));
    }

    @Test
    @DisplayName("Should not match incorrect password")
    void shouldNotMatchIncorrectPassword() {
        String rawPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertFalse(PasswordEncoder.matches(wrongPassword, encoded));
    }

    @Test
    @DisplayName("Should not match empty password with encoded")
    void shouldNotMatchEmptyPasswordWithEncoded() {
        String rawPassword = "password123";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertFalse(PasswordEncoder.matches("", encoded));
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        String rawPassword = "P@$$w0rd!#&*()";
        String encoded = PasswordEncoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertTrue(PasswordEncoder.matches(rawPassword, encoded));
        assertFalse(PasswordEncoder.matches("P@$$w0rd", encoded));
    }

    @Test
    @DisplayName("Should handle long passwords")
    void shouldHandleLongPasswords() {
        String longPassword = "a".repeat(100);
        String encoded = PasswordEncoder.encode(longPassword);
        
        assertNotNull(encoded);
        assertTrue(PasswordEncoder.matches(longPassword, encoded));
    }
}
